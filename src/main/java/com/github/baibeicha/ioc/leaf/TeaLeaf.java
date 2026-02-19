package com.github.baibeicha.ioc.leaf;

import com.github.baibeicha.ioc.reflection.Dependency;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TeaLeaf {

    private String name;
    private final Object instance;
    private final Class<?> type;
    private final LeafScope scope;
    private int priority;

    public TeaLeaf(String name, Object instance, Class<?> type) {
        this.name = name;
        this.instance = instance;
        this.type = type;
        this.scope = LeafScope.SINGLETON;
        this.priority = 10;
    }

    public TeaLeaf(String name, Object instance, Class<?> type, LeafScope scope,
                   int priority, String initMethod, String destroyMethod) {
        this.name = name;
        this.instance = instance;
        this.type = type;
        this.scope = scope;
        this.priority = priority;
    }

    public TeaLeaf(Definition definition, Object instance) {
        this.name = definition.getName();
        this.instance = instance;
        this.type = definition.getType();
        this.scope = definition.getScope();
        this.priority = definition.getPriority();
    }

    public String name() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <Type> Type instance() {
        if (instance == null) {
            return null;
        }
        return (Type) instance;
    }

    public Class<?> type() {
        return type;
    }

    public LeafScope scope() {
        return scope;
    }

    public int priority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class Definition {

        private String name;
        private Class<?> type;
        private LeafScope scope;
        private int priority;
        private List<Dependency> dependencies;
        private List<Dependency> optionalDependencies;
        private Method initMethod;
        private Method destroyMethod;
        private Method fabricMethod;
        private Class<?> fabricMethodConfigClass;
        private Constructor<?> fabricConstructor;
        private FabricType fabricType;
        private Object instance;

        public enum FabricType {
            METHOD, CONSTRUCTOR, INSTANCE
        }

        public Object invoke(Object... args) throws NoSuchMethodException, InvocationTargetException,
                InstantiationException, IllegalAccessException {
            return switch (fabricType) {
                case METHOD -> fabricMethod.invoke(
                        fabricMethodConfigClass.getConstructor().newInstance(),
                        args
                );
                case CONSTRUCTOR -> fabricConstructor.newInstance(args);
                case INSTANCE -> instance;
            };
        }

        public Object getInstance() {
            return instance;
        }

        public void setInstance(Object instance) {
            this.instance = instance;
        }

        public Class<?> getFabricMethodConfigClass() {
            return fabricMethodConfigClass;
        }

        public void setFabricMethodConfigClass(Class<?> fabricMethodConfigClass) {
            this.fabricMethodConfigClass = fabricMethodConfigClass;
        }

        public FabricType getFabricType() {
            return fabricType;
        }

        public void setFabricType(FabricType fabricType) {
            this.fabricType = fabricType;
        }

        public Method getFabricMethod() {
            return fabricMethod;
        }

        public void setFabricMethod(Method fabricMethod) {
            this.fabricMethod = fabricMethod;
        }

        public Constructor<?> getFabricConstructor() {
            return fabricConstructor;
        }

        public void setFabricConstructor(Constructor<?> fabricConstructor) {
            this.fabricConstructor = fabricConstructor;
        }

        public Method getInitMethod() {
            return initMethod;
        }

        public void setInitMethod(Method initMethod) {
            this.initMethod = initMethod;
        }

        public Method getDestroyMethod() {
            return destroyMethod;
        }

        public void setDestroyMethod(Method destroyMethod) {
            this.destroyMethod = destroyMethod;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public List<Dependency> getDependencies() {
            return dependencies;
        }

        public void setDependencies(Collection<Dependency> dependencies) {
            if (dependencies == null) {
                this.dependencies = new ArrayList<>();
            } else {
                this.dependencies = new ArrayList<>(dependencies);
            }
        }

        public void addDependencies(Dependency... dependencies) {
            if (this.dependencies == null) {
                this.dependencies = new ArrayList<>(dependencies.length);
            }

            this.dependencies.addAll(Arrays.asList(dependencies));
        }

        public void addDependencies(List<Dependency> dependencies) {
            if (this.dependencies == null) {
                this.dependencies = new ArrayList<>(dependencies.size());
            }

            this.dependencies.addAll(dependencies);
        }

        public List<Dependency> getOptionalDependencies() {
            return optionalDependencies;
        }

        public void setOptionalDependencies(Collection<Dependency> optionalDependencies) {
            if (optionalDependencies == null) {
                this.optionalDependencies = new ArrayList<>();
            } else {
                this.optionalDependencies = new ArrayList<>(optionalDependencies);
            }
        }

        public void addOptionalDependencies(Dependency... optionalDependencies) {
            if (this.optionalDependencies == null) {
                this.optionalDependencies = new ArrayList<>(optionalDependencies.length);
            }

            this.optionalDependencies.addAll(Arrays.asList(optionalDependencies));
        }

        public void addOptionalDependencies(List<Dependency> optionalDependencies) {
            if (this.optionalDependencies == null) {
                this.optionalDependencies = new ArrayList<>(optionalDependencies.size());
            }

            this.optionalDependencies.addAll(optionalDependencies);
        }

        public LeafScope getScope() {
            return scope;
        }

        public void setScope(LeafScope scope) {
            this.scope = scope;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        @Override
        public String toString() {
            return "Definition[name=" + name + ", type=" + type + ", scope=" + scope + ", priority=" + priority + "]";
        }
    }
}
