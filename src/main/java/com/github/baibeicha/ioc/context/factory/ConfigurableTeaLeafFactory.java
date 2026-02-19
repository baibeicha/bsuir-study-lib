package com.github.baibeicha.ioc.context.factory;

import com.github.baibeicha.ioc.context.exception.CycleDependenciesWhileCreatingTeaLeafException;
import com.github.baibeicha.ioc.context.exception.ExistsMoreThanOneTeaLeafException;
import com.github.baibeicha.ioc.context.exception.FailedToCreateTeaLeafException;
import com.github.baibeicha.ioc.context.exception.FailedToDestroyTeaLeafException;
import com.github.baibeicha.ioc.context.exception.FailedToInitTeaLeafException;
import com.github.baibeicha.ioc.context.exception.TeaLeafNotFoundException;
import com.github.baibeicha.ioc.context.factory.aware.NameAware;
import com.github.baibeicha.ioc.context.factory.aware.PriorityAware;
import com.github.baibeicha.ioc.context.registry.TeaLeafDefinitionRegistry;
import com.github.baibeicha.ioc.leaf.LeafScope;
import com.github.baibeicha.ioc.leaf.TeaLeaf;
import com.github.baibeicha.ioc.reflection.Dependency;
import com.github.baibeicha.util.PrintCycle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigurableTeaLeafFactory implements TeaLeafFactory {

    protected final Map<Class<?>, List<TeaLeaf>> leafs;
    protected final Map<String, TeaLeaf> leafNames;
    protected final Map<String, CreationStage> creationStages;
    protected final TeaLeafDefinitionRegistry definitionRegistry;
    protected final List<String> createTeaLeafStack;

    protected enum CreationStage {
        NOT_CREATED, CREATING, CREATED
    }

    public ConfigurableTeaLeafFactory(Map<Class<?>, List<TeaLeaf>> leafs, Map<String, TeaLeaf> leafNames,
                                      TeaLeafDefinitionRegistry definitionRegistry) {
        this.leafs = leafs;
        this.leafNames = leafNames;
        this.definitionRegistry = definitionRegistry;
        this.creationStages = new LinkedHashMap<>();
        this.createTeaLeafStack = new ArrayList<>();
    }

    @Override
    public TeaLeaf createTeaLeaf(TeaLeaf.Definition definition) {
        createTeaLeafStack.addLast(definition.getName() + ":" + definition.getType().getName());
        if (creationStages.get(definition.getName()) == CreationStage.CREATING) {
            throw new CycleDependenciesWhileCreatingTeaLeafException(
                    PrintCycle.buildCycleMessage(createTeaLeafStack)
            );
        } else if (creationStages.get(definition.getName()) == CreationStage.CREATED) {
            createTeaLeafStack.removeLast();
            return leafNames.get(definition.getName());
        } else {
            creationStages.put(definition.getName(), CreationStage.CREATING);
        }

        Object instance;

        List<Dependency> dependencies = definition.getDependencies();
        Object[] dependenciesInstances = new Object[dependencies.size()];

        for (int i = 0; i < dependencies.size(); i++) {
            Dependency dependency = dependencies.get(i);
            TeaLeaf.Definition dependencyDefinition = getDependencyDefinition(dependency);
            Object dependencyInstance = createTeaLeaf(dependencyDefinition).instance();
            dependenciesInstances[i] = dependencyInstance;
        }

        try {
            instance = definition.invoke(dependenciesInstances);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new FailedToCreateTeaLeafException(e);
        }

        List<Dependency> optionalDependencies = definition.getOptionalDependencies();
        for (Dependency dependency : optionalDependencies) {
            TeaLeaf.Definition dependencyDefinition = getDependencyDefinition(dependency);
            try {
                Object value = createTeaLeaf(dependencyDefinition).instance();
                if (dependency.method() != null) {
                    dependency.method().invoke(instance, value);
                } else if (dependency.field() != null) {
                    dependency.field().set(instance, value);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
                throw new FailedToCreateTeaLeafException(e);
            }
        }

        TeaLeaf leaf = new TeaLeaf(definition, instance);

        invokeAware(leaf);
        initLeaf(leaf, definition);

        createTeaLeafStack.removeLast();
        return register(leaf);
    }

    protected TeaLeaf.Definition getDependencyDefinition(Dependency dependency) {
        TeaLeaf.Definition dependencyDefinition;
        List<TeaLeaf.Definition> definitionList = definitionRegistry.getDefinition(dependency.type());
        if (definitionList.size() > 1) {
            dependencyDefinition = definitionList.stream()
                    .filter(def -> def.getName().equals(dependency.name()))
                    .max(Comparator.comparingInt(TeaLeaf.Definition::getPriority))
                    .orElseThrow(() -> new TeaLeafNotFoundException(
                            "Exists " + definitionList.size() +
                                    " TeaLeafs with " + dependency.type() +
                                    " but 0 with name " + dependency.name()
                    ));
        } else {
            dependencyDefinition = definitionList.getFirst();
        }

        return dependencyDefinition;
    }

    @Override
    public TeaLeaf register(TeaLeaf leaf) {
        if (leaf.scope() == LeafScope.SINGLETON) {
            creationStages.put(leaf.name(), CreationStage.CREATED);
            leafNames.put(leaf.name(), leaf);
            addToLeafs(leaf);
        } else {
            creationStages.put(leaf.name(), CreationStage.NOT_CREATED);
        }
        return leaf;
    }

    @Override
    public Object getTeaLeaf(String name) {
        Object value = leafNames.get(name);
        if (value == null) {
            throw new TeaLeafNotFoundException("No such tea leaf: " + name);
        }
        return value;
    }

    @Override
    public <Type> Type getTeaLeaf(Class<Type> type) {
        List<TeaLeaf> leafsList = leafs.get(type);

        if (leafsList == null || leafsList.isEmpty()) {
            throw new TeaLeafNotFoundException("No such tea leaf: " + type);
        } else if (leafsList.size() > 1) {
            throw new ExistsMoreThanOneTeaLeafException(
                    "Exists " + leafsList.size() + " TeaLeafs for class " + type.getName()
            );
        }

        return leafsList.getFirst().instance();
    }

    @Override
    public <Type> Type getTeaLeaf(Class<Type> type, String name) {
        return leafs.get(type).stream()
                .filter(leaf -> leaf.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new TeaLeafNotFoundException("No such tea leaf: " + name))
                .instance();
    }

    @Override
    public <Type> List<Type> getTeaLeafs(Class<Type> type) {
        List<TeaLeaf> leafsList = leafs.get(type).stream()
                .sorted((l1, l2) -> Integer.compare(l2.priority(), l1.priority()))
                .toList();

        if (leafsList.isEmpty()) {
            throw new TeaLeafNotFoundException("No such tea leaf: " + type);
        }

        List<Type> result = new ArrayList<>(leafsList.size());
        for (TeaLeaf leaf : leafsList) {
            result.add(leaf.instance());
        }

        return result;
    }

    @Override
    public void clear() {
        for (String leafName : leafNames.keySet()) {
            TeaLeaf.Definition definition = definitionRegistry.getDefinition(leafName);
            TeaLeaf leaf = leafNames.get(leafName);
            if (leaf != null && definition != null) {
                destroyLeaf(leaf, definition);
                creationStages.put(leafName, CreationStage.NOT_CREATED);
            }
        }

        for (List<TeaLeaf> list : leafs.values()) {
            list.clear();
        }
        leafs.clear();
        leafNames.clear();
    }

    protected void invokeAware(TeaLeaf leaf) {
        if (leaf.instance().getClass().isAssignableFrom(NameAware.class)) {
            NameAware nameAware = leaf.instance();
            String newName = nameAware.setName(leaf.name());
            leaf.setName(newName);
        }
        if (leaf.instance().getClass().isAssignableFrom(PriorityAware.class)) {
            PriorityAware priorityAware = leaf.instance();
            int newPriority = priorityAware.setPriority(leaf.priority());
            leaf.setPriority(newPriority);
        }
    }

    protected void initLeaf(TeaLeaf leaf, TeaLeaf.Definition definition) {
        try {
            if (definition.getInitMethod() != null) {
                definition.getInitMethod().invoke(leaf.instance());
            }
        } catch (Exception e) {
            throw new FailedToInitTeaLeafException(e);
        }
    }

    protected void destroyLeaf(TeaLeaf leaf, TeaLeaf.Definition definition) {
        try {
            if (definition.getDestroyMethod() != null) {
                definition.getDestroyMethod().invoke(leaf.instance());
            }
        } catch (Exception e) {
            throw new FailedToDestroyTeaLeafException(e);
        }
    }

    protected void addToLeafs(TeaLeaf leaf) {
        List<TeaLeaf> list = leafs.get(leaf.type());
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(leaf);
        leafs.put(leaf.type(), list);
    }

    @Override
    public Map<String, TeaLeaf> getTeaLeafs() {
        return leafNames;
    }
}
