package com.github.baibeicha.ioc.reflection;

import com.github.baibeicha.ioc.annotation.leaf.FillIn;
import com.github.baibeicha.ioc.annotation.leaf.Qualifier;
import com.github.baibeicha.ioc.reflection.exception.AnnotatedConstructorNotFoundException;
import com.github.baibeicha.reflection.util.AnnotationUtils;
import com.github.baibeicha.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassDependenciesScanner {

    public Set<Dependency> scanForConstructorDependencies(Class<?> clazz) {
        Set<Dependency> dependencies = new LinkedHashSet<>();

        Constructor<?> constructor = findFillInConstructor(clazz);
        addConstructorDependencies(constructor, dependencies);

        return dependencies;
    }

    public Constructor<?> findFillInConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new AnnotatedConstructorNotFoundException(
                    "You have 0 constructors in class " + clazz.getName()
            );
        } else if (constructors.length == 1) {
            return constructors[0];
        } else {
            return Arrays.stream(constructors)
                    .filter(constr -> AnnotationUtils.isAnnotated(constr, FillIn.class))
                    .findFirst()
                    .orElseThrow(() -> new AnnotatedConstructorNotFoundException(
                                    StringUtils.format("You have more than 1 constructor ({}) in class {}",
                                            constructors.length, clazz.getName()
                                    )
                            )
                    );
        }
    }

    protected void addConstructorDependencies(Constructor<?> constructor, Set<Dependency> dependencies) {
        constructor.setAccessible(true);
        for (Parameter param : constructor.getParameters()) {
            addParameterDependency(dependencies, param);
        }
    }

    protected void addParameterDependency(Set<Dependency> dependencies, Parameter param) {
        String qualifierName = qualifierName(param);
        String paramName = qualifierName == null ? param.getName() : qualifierName;
        Dependency dependency = new Dependency(paramName, param.getType());
        dependencies.add(dependency);
    }

    protected String qualifierName(AnnotatedElement element) {
        if (element.isAnnotationPresent(Qualifier.class)) {
            Qualifier qualifier = element.getAnnotation(Qualifier.class);
            return qualifier.value();
        } else {
            return null;
        }
    }

    public Set<Dependency> scanForFieldDependencies(Class<?> clazz) {
        Set<Dependency> dependencies = new LinkedHashSet<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (AnnotationUtils.isAnnotated(field, FillIn.class)) {
                field.setAccessible(true);
                String qualifierName = qualifierName(field);
                String fieldName = qualifierName == null ? field.getName() : qualifierName;
                Dependency dependency = new Dependency(fieldName, field.getType());
                dependency.setField(field);
                dependencies.add(dependency);
            }
        }

        return dependencies;
    }

    public Set<Dependency> scanForMethodDependencies(Class<?> clazz) {
        Set<Dependency> dependencies = new LinkedHashSet<>();

        List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> AnnotationUtils.isAnnotated(method, FillIn.class))
                .toList();
        for (Method method : methods) {
            addMethodDependencies(dependencies, method);
        }

        return dependencies;
    }

    public Set<Dependency> scanForLeafFactoryMethodDependencies(Method method) {
        Set<Dependency> dependencies = new LinkedHashSet<>();

        method.setAccessible(true);
        for (Parameter param : method.getParameters()) {
            String qualifierName = qualifierName(param);
            String name = qualifierName == null ? param.getName() : qualifierName;
            Class<?> type = param.getType();
            Dependency dependency = new Dependency(name, type);
            dependencies.add(dependency);
        }

        return dependencies;
    }

    protected void addMethodDependencies(Set<Dependency> dependencies, Method method) {
        method.setAccessible(true);
        for (Parameter param : method.getParameters()) {
            String qualifierName = qualifierName(param);
            String paramName = qualifierName == null ? param.getName() : qualifierName;
            Dependency dependency = new Dependency(paramName, param.getType());
            dependency.setMethod(method);
            dependencies.add(dependency);
        }
    }
}
