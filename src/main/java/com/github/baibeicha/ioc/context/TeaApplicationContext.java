package com.github.baibeicha.ioc.context;

import com.github.baibeicha.ioc.TeaApplication;
import com.github.baibeicha.ioc.annotation.configuration.Configuration;
import com.github.baibeicha.ioc.annotation.leaf.Priority;
import com.github.baibeicha.ioc.annotation.leaf.Scope;
import com.github.baibeicha.ioc.application.event.context.EventContext;
import com.github.baibeicha.ioc.context.factory.ConfigurableTeaLeafFactory;
import com.github.baibeicha.ioc.context.factory.TeaLeafFactory;
import com.github.baibeicha.ioc.context.registry.MapTeaLeafDefinitionRegistry;
import com.github.baibeicha.ioc.context.registry.TeaLeafDefinitionRegistry;
import com.github.baibeicha.ioc.leaf.LeafScope;
import com.github.baibeicha.ioc.leaf.TeaLeaf;
import com.github.baibeicha.ioc.reflection.ClassDependenciesScanner;
import com.github.baibeicha.ioc.reflection.Dependency;
import com.github.baibeicha.reflection.PackageScanner;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeaApplicationContext {

    protected final Class<?> configClass;
    protected final TeaApplication.ContextConfiguration config;
    protected final PackageScanner packageScanner;
    protected final ClassDependenciesScanner classDependenciesScanner;
    protected final TeaLeafFactory leafFactory;
    protected final TeaLeafDefinitionRegistry definitionRegistry;
    protected final EventContext eventContext;

    protected boolean started;

    public TeaApplicationContext(Class<?> configClass, PackageScanner scanner,
                                 TeaApplication.ContextConfiguration config,
                                 ClassDependenciesScanner dependenciesScanner,
                                 EventContext eventContext) {
        this.configClass = configClass;
        this.config = config;
        this.packageScanner = scanner;
        this.classDependenciesScanner = dependenciesScanner;
        this.definitionRegistry = new MapTeaLeafDefinitionRegistry();
        this.eventContext = eventContext;
        this.leafFactory = new ConfigurableTeaLeafFactory(
                new HashMap<>(), new HashMap<>(), definitionRegistry
        );
        this.started = false;
    }

    public void start() {
        if (!started) {
            List<TeaLeaf.Definition> definitions = scanPackages();
            initLeafs(definitions);
            started = true;
        }
    }

    public Object getTeaLeaf(String name) {
        return leafFactory.getTeaLeaf(name);
    }

    public <Type> Type getTeaLeaf(Class<Type> type) {
        return leafFactory.getTeaLeaf(type);
    }

    public <Type> Type getTeaLeaf(Class<Type> type, String name) {
        return leafFactory.getTeaLeaf(type, name);
    }

    public <Type> List<Type> getTeaLeafs(Class<Type> type) {
        return leafFactory.getTeaLeafs(type);
    }

    public <Type> Type registerTeaLeaf(Class<Type> clazz, String name) {
        TeaLeaf.Definition definition = new TeaLeaf.Definition();
        definition.setName(name);
        definition.setType(clazz);
        definition.setScope(LeafScope.SINGLETON);
        definition.setPriority(10);
        definition.setInitMethod(null);
        definition.setDestroyMethod(null);
        definition.setFabricType(TeaLeaf.Definition.FabricType.CONSTRUCTOR);

        definition.setDependencies(classDependenciesScanner.scanForConstructorDependencies(clazz));
        Set<Dependency> optionalDependencies = new HashSet<>();
        optionalDependencies.addAll(classDependenciesScanner.scanForMethodDependencies(clazz));
        optionalDependencies.addAll(classDependenciesScanner.scanForFieldDependencies(clazz));
        definition.setOptionalDependencies(optionalDependencies);

        definition.setFabricConstructor(classDependenciesScanner.findFillInConstructor(clazz));

        return leafFactory.createTeaLeaf(definition).instance();
    }

    public void close() {
        leafFactory.clear();
        started = false;
    }

    protected void initLeafs(List<TeaLeaf.Definition> definitions) {
        definitionRegistry.addAllDefinitions(definitions);
        for (TeaLeaf.Definition definition : definitions.stream()
                .filter(definition -> definition.getScope() == LeafScope.SINGLETON)
                .toList()) {
            leafFactory.createTeaLeaf(definition);
        }
    }

    protected List<TeaLeaf.Definition> scanPackages() {
        Set<Class<?>> teaLeafAnnotatedClasses = findTeaLeafAnnotatedClasses();
        Set<Class<?>> configurationAnnotatedClasses = findConfigurationAnnotatedClasses();

        List<TeaLeaf.Definition> classDefinitions = teaLeafAnnotatedClasses.stream()
                .map(this::getDefinitionFromClass)
                .toList();

        List<TeaLeaf.Definition> methodDefinitions = new ArrayList<>();
        for (Class<?> configurationAnnotatedClass : configurationAnnotatedClasses) {
            methodDefinitions.addAll(getDefinitionFromAnnotatedMethod(configurationAnnotatedClass));
        }

        List<TeaLeaf.Definition> definitions = new ArrayList<>(
                classDefinitions.size() + methodDefinitions.size() + 1);
        definitions.addFirst(createDefinitionOfApplicationContext());
        definitions.addAll(classDefinitions);
        definitions.addAll(methodDefinitions);

        return definitions;
    }

    protected TeaLeaf.Definition createDefinitionOfApplicationContext() {
        TeaLeaf.Definition definition = new TeaLeaf.Definition();
        definition.setPriority(Integer.MAX_VALUE);
        definition.setScope(LeafScope.SINGLETON);
        definition.setName("teaApplicationContext");
        definition.setInstance(this);
        definition.setFabricType(TeaLeaf.Definition.FabricType.INSTANCE);
        definition.setDependencies(Collections.emptyList());
        definition.setOptionalDependencies(Collections.emptyList());
        definition.setType(TeaApplicationContext.class);
        definition.setInitMethod(null);
        definition.setDestroyMethod(null);
        return definition;
    }

    protected List<TeaLeaf.Definition> getDefinitionFromAnnotatedMethod(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.
                        isAnnotationPresent(com.github.baibeicha.ioc.annotation.leaf.TeaLeaf.class)
                )
                .map(method -> getDefinitionFromMethod(method, clazz))
                .toList();
    }

    protected TeaLeaf.Definition getDefinitionFromMethod(Method method, Class<?> configClass) {
        TeaLeaf.Definition definition = new TeaLeaf.Definition();

        String name = method.getName();
        name = getDefinitionName(method, name);
        definition.setName(name);

        definition.setType(method.getReturnType());

        definition.setScope(getDefinitionScope(method));

        definition.setPriority(getDefinitionPriority(method));

        Set<Dependency> dependencies =
                classDependenciesScanner.scanForLeafFactoryMethodDependencies(method);
        definition.setDependencies(dependencies);
        definition.setOptionalDependencies(null);

        definition.setFabricType(TeaLeaf.Definition.FabricType.METHOD);
        definition.setFabricMethod(method);

        Class<?> clazz = definition.getType();
        String initMethodName = getDefinitionInitMethod(method);
        Method initMethod = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(initMethodName))
                .findFirst()
                .orElse(null);
        definition.setInitMethod(initMethod);

        String destroyMethodName = getDefinitionDestroyMethod(method);
        Method destroyMethod = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(destroyMethodName))
                .findFirst()
                .orElse(null);
        definition.setDestroyMethod(destroyMethod);

        definition.setFabricMethodConfigClass(configClass);

        return definition;
    }

    protected TeaLeaf.Definition getDefinitionFromClass(Class<?> clazz) {
        TeaLeaf.Definition definition = new TeaLeaf.Definition();

        String name = clazz.getSimpleName();
        name = getDefinitionName(clazz, name);
        definition.setName(name);

        definition.setType(clazz);

        definition.setScope(getDefinitionScope(clazz));

        definition.setPriority(getDefinitionPriority(clazz));

        Set<Dependency> dependencies = classDependenciesScanner.scanForConstructorDependencies(clazz);
        Set<Dependency> optionalDependencies = new HashSet<>();
        optionalDependencies.addAll(classDependenciesScanner.scanForMethodDependencies(clazz));
        optionalDependencies.addAll(classDependenciesScanner.scanForFieldDependencies(clazz));

        definition.setDependencies(dependencies);
        definition.setOptionalDependencies(optionalDependencies);

        definition.setFabricType(TeaLeaf.Definition.FabricType.CONSTRUCTOR);
        definition.setFabricConstructor(classDependenciesScanner.findFillInConstructor(clazz));

        String initMethodName = getDefinitionInitMethod(clazz);
        Method initMethod = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().equals(initMethodName))
                .findFirst()
                .orElse(null);
        definition.setInitMethod(initMethod);

        String destroyMethodName = getDefinitionDestroyMethod(clazz);
        Method destroyMethod = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().equals(destroyMethodName))
                .findFirst()
                .orElse(null);
        definition.setDestroyMethod(destroyMethod);

        return definition;
    }

    protected String getDefinitionInitMethod(AnnotatedElement element) {
        String initMethodName = "init";
        com.github.baibeicha.ioc.annotation.leaf.TeaLeaf leaf =
                element.getAnnotation(com.github.baibeicha.ioc.annotation.leaf.TeaLeaf.class);
        if (!leaf.initMethod().isEmpty()) {
            initMethodName = leaf.initMethod();
        }
        return initMethodName;
    }

    protected String getDefinitionDestroyMethod(AnnotatedElement element) {
        String destroyMethodName = "destroy";
        com.github.baibeicha.ioc.annotation.leaf.TeaLeaf leaf =
                element.getAnnotation(com.github.baibeicha.ioc.annotation.leaf.TeaLeaf.class);
        if (!leaf.destroyMethod().isEmpty()) {
            destroyMethodName = leaf.destroyMethod();
        }
        return destroyMethodName;
    }

    protected String getDefinitionName(AnnotatedElement element, String name) {
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        com.github.baibeicha.ioc.annotation.leaf.TeaLeaf leaf =
                element.getAnnotation(com.github.baibeicha.ioc.annotation.leaf.TeaLeaf.class);

        name = leaf.name().isEmpty() ? name : leaf.name();
        return name;
    }

    protected LeafScope getDefinitionScope(AnnotatedElement element) {
        LeafScope scope = LeafScope.SINGLETON;
        if (element.isAnnotationPresent(Scope.class)) {
            scope = element.getAnnotation(Scope.class).value();
        }
        return scope;
    }

    protected int getDefinitionPriority(AnnotatedElement element) {
        int priority = 10;
        if (element.isAnnotationPresent(Priority.class)) {
            priority = element.getAnnotation(Priority.class).value();
        }
        return priority;
    }

    protected Set<Class<?>> findConfigurationAnnotatedClasses() {
        Set<Class<?>> classes = new HashSet<>();
        for (String pack : config.packages) {
            classes.addAll(packageScanner.findAllClassesWithAnnotation(
                    pack, Configuration.class)
            );
        }
        return classes;
    }

    protected Set<Class<?>> findTeaLeafAnnotatedClasses() {
        Set<Class<?>> classes = new HashSet<>();
        for (String pack : config.packages) {
            classes.addAll(packageScanner.findAllClassesWithAnnotation(
                    pack, com.github.baibeicha.ioc.annotation.leaf.TeaLeaf.class)
            );
        }
        return classes;
    }

    public PackageScanner getPackageScanner() {
        return packageScanner;
    }

    public ClassDependenciesScanner getClassDependenciesScanner() {
        return classDependenciesScanner;
    }

    public TeaLeafFactory getLeafFactory() {
        return leafFactory;
    }

    public TeaLeafDefinitionRegistry getDefinitionRegistry() {
        return definitionRegistry;
    }

    public EventContext getEventContext() {
        return eventContext;
    }
}
