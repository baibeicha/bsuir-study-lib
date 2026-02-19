package com.github.baibeicha.reflection.util;

import com.github.baibeicha.reflection.annotation.AliasFor;
import com.github.baibeicha.reflection.exception.AnnotationConfigurationException;
import com.github.baibeicha.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationUtils {

    private static final Map<Class<? extends Annotation>, Map<String, Method>>
            ANNOTATION_ATTRIBUTES_CACHE = new ConcurrentHashMap<>();
    private static final Map<CacheKey, Annotation> FIND_CACHE = new ConcurrentHashMap<>();

    private record CacheKey(AnnotatedElement element, Class<? extends Annotation> annotationType) {
    }

    /**
     * Determines if an annotation of {@code annotationType} is present on the specified
     * {@code element} or within its annotation hierarchy (as a meta-annotation).
     * <p>
     * This method provides a highly efficient check and does not create a synthesized
     * annotation proxy.
     *
     * @param element        the annotated element to check.
     * @param annotationType the annotation type to look for.
     * @return {@code true} if the annotation is present, {@code false} otherwise.
     */
    public static boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
        return hasAnnotationInternal(element, annotationType, new HashSet<>());
    }

    private static boolean hasAnnotationInternal(
            AnnotatedElement element,
            Class<? extends Annotation> annotationType,
            Set<Annotation> visited) {

        if (element.isAnnotationPresent(annotationType)) {
            return true;
        }

        for (Annotation metaAnn : element.getAnnotations()) {
            if (!visited.add(metaAnn)) {
                continue;
            }

            if (hasAnnotationInternal(metaAnn.annotationType(), annotationType, visited)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds the first annotation of {@code annotationType} within the annotation hierarchy
     * of the specified {@code element}.
     * The returned annotation is a "synthesized" proxy that transparently handles
     * attribute aliases defined by {@link AliasFor}.
     *
     * @param element        The annotated element.
     * @param annotationType The type of annotation to find.
     * @return The synthesized annotation, or {@code null} if not found.
     */
    public static <A extends Annotation> A findAnnotation(AnnotatedElement element, Class<A> annotationType) {
        CacheKey key = new CacheKey(element, annotationType);
        Annotation cached = FIND_CACHE.get(key);
        if (cached != null) {
            return annotationType.cast(cached);
        }

        A result = findAnnotationInternal(element, annotationType, new HashSet<>());
        if (result != null) {
            FIND_CACHE.put(key, result);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A findAnnotationInternal(
            AnnotatedElement element, Class<A> annotationType, Set<Annotation> visited) {
        A direct = element.getAnnotation(annotationType);
        if (direct != null) {
            return (A) synthesize(direct, new HashSet<>());
        }

        for (Annotation metaAnn : element.getAnnotations()) {
            if (!visited.add(metaAnn)) {
                continue;
            }
            A found = findAnnotationInternal(metaAnn.annotationType(), annotationType, visited);
            if (found != null) {
                return (A) synthesize(found, metaAnn, new HashSet<>());
            }
        }
        return null;
    }

    private static Map<String, Method> getAttributeMethods(Class<? extends Annotation> annotationType) {
        return ANNOTATION_ATTRIBUTES_CACHE.computeIfAbsent(annotationType, type -> {
            Map<String, Method> map = new LinkedHashMap<>();
            for (Method method : type.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getReturnType() != void.class) {
                    map.put(method.getName(), method);
                }
            }
            return map;
        });
    }

    private static Annotation synthesize(Annotation annotation, Set<Annotation> visited) {
        return synthesize(annotation, null, visited);
    }

    private static Annotation synthesize(Annotation annotationToSynthesize,
                                         Annotation source,
                                         Set<Annotation> visited) {
        if (source == null) {
            source = annotationToSynthesize;
        }

        Class<? extends Annotation> type = annotationToSynthesize.annotationType();
        Map<String, Method> attributes = getAttributeMethods(type);
        Map<String, Object> valueMap = new HashMap<>();

        for (Map.Entry<String, Method> entry : attributes.entrySet()) {
            try {
                valueMap.put(entry.getKey(), entry.getValue().invoke(source));
            } catch (Exception e) {
                throw new IllegalStateException(
                        StringUtils.format("Failed to read attribute {} from {}", entry.getKey(), source), e
                );
            }
        }

        Set<Annotation> processingVisited = new HashSet<>();
        resolveMetaAliases(type, valueMap, attributes, processingVisited);

        resolveIntraAnnotationAliases(type, valueMap, attributes);

        Annotation finalSource = source;
        return (Annotation) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(finalSource, args);
                    }
                    if (method.getName().equals("annotationType")) {
                        return type;
                    }
                    Object value = valueMap.get(method.getName());
                    if (value == null && !valueMap.containsKey(method.getName())) {
                        return method.getDefaultValue();
                    }
                    return value;
                });
    }

    private static boolean isDefaultValue(Object value, Method attributeMethod) {
        if (value == null) return true;
        return Objects.equals(value, attributeMethod.getDefaultValue());
    }

    private static void resolveIntraAnnotationAliases(Class<? extends Annotation> type,
                                                      Map<String, Object> valueMap,
                                                      Map<String, Method> attributes) {
        List<Set<String>> aliasGroups = new ArrayList<>();
        Map<String, String> attributeToRoot = new HashMap<>();

        for (String attrName : attributes.keySet()) {
            if (attributeToRoot.containsKey(attrName)) continue;

            Set<String> group = new HashSet<>();
            String current = attrName;
            while (current != null && group.add(current)) {
                AliasFor aliasFor = attributes.get(current).getAnnotation(AliasFor.class);
                String next = null;
                if (aliasFor != null
                        && aliasFor.annotation() == Annotation.class
                        && !aliasFor.value().isEmpty()) {
                    next = aliasFor.value();
                    if (!attributes.containsKey(next)) {
                        throw new AnnotationConfigurationException(String.format(
                                "@AliasFor declaration on %s.%s points to a non-existent attribute '%s'",
                                type.getName(), current, next));
                    }
                }
                current = next;
            }
            aliasGroups.add(group);
            String root = group.iterator().next();
            for (String member : group) {
                attributeToRoot.put(member, root);
            }
        }

        for (Set<String> group : aliasGroups) {
            Object nonDefaultValue = null;
            String nonDefaultAttribute = null;

            for (String attrName : group) {
                Object value = valueMap.get(attrName);
                if (!isDefaultValue(value, attributes.get(attrName))) {
                    if (nonDefaultValue != null && !Objects.equals(nonDefaultValue, value)) {
                        throw new AnnotationConfigurationException(String.format(
                                "Conflicting values for aliased attributes %s in %s: %s on %s vs %s on %s",
                                group, type.getName(), nonDefaultValue, nonDefaultAttribute, value, attrName));
                    }
                    nonDefaultValue = value;
                    nonDefaultAttribute = attrName;
                }
            }

            if (nonDefaultValue != null) {
                for (String attrName : group) {
                    valueMap.put(attrName, nonDefaultValue);
                }
            }
        }
    }

    private static void resolveMetaAliases(Class<? extends Annotation> type,
                                           Map<String, Object> valueMap,
                                           Map<String, Method> attributes,
                                           Set<Annotation> visited) {
        for (Map.Entry<String, Method> entry : attributes.entrySet()) {
            String attrName = entry.getKey();
            Method method = entry.getValue();
            AliasFor aliasFor = method.getAnnotation(AliasFor.class);

            if (aliasFor == null || aliasFor.annotation() == Annotation.class) {
                continue;
            }

            Class<? extends Annotation> targetAnnotationType = aliasFor.annotation();
            Annotation targetAnnotation = type.getAnnotation(targetAnnotationType);
            if (targetAnnotation == null) continue;

            String targetAttrName = aliasFor.attribute().isEmpty() ? attrName : aliasFor.attribute();

            try {
                Method targetMethod = targetAnnotationType.getMethod(targetAttrName);
                Object targetValue = targetMethod.invoke(synthesize(targetAnnotation, visited));

                Object currentValue = valueMap.get(attrName);
                if (isDefaultValue(currentValue, method)) {
                    valueMap.put(attrName, targetValue);
                } else if (!Objects.equals(currentValue, targetValue)) {
                    throw new AnnotationConfigurationException(String.format(
                            "Conflicting values for attribute '%s' in annotation %s: " +
                                    "explicit value %s and meta-annotation value %s",
                            attrName, type.getName(), currentValue, targetValue));
                }
            } catch (NoSuchMethodException e) {
                throw new AnnotationConfigurationException(String.format(
                        "@AliasFor on %s.%s points to non-existent attribute '%s' in @%s",
                        type.getName(), attrName, targetAttrName, targetAnnotationType.getName()));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to resolve meta-alias", e);
            }
        }
    }
}
