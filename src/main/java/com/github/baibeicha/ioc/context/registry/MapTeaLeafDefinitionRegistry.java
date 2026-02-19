package com.github.baibeicha.ioc.context.registry;

import com.github.baibeicha.ioc.leaf.TeaLeaf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapTeaLeafDefinitionRegistry implements TeaLeafDefinitionRegistry {

    private final Map<String, TeaLeaf.Definition> definitions;
    private final Map<Class<?>, List<TeaLeaf.Definition>> definitionsByType;

    public MapTeaLeafDefinitionRegistry() {
        definitions = new HashMap<>();
        definitionsByType = new HashMap<>();
    }

    @Override
    public TeaLeaf.Definition getDefinition(final String name) {
        return definitions.get(name);
    }

    @Override
    public List<TeaLeaf.Definition> getDefinition(final Class<?> type) {
        return definitionsByType.get(type);
    }

    @Override
    public void addDefinition(final TeaLeaf.Definition definition) {
        definitions.put(definition.getName(), definition);

        List<TeaLeaf.Definition> definitions = definitionsByType.get(definition.getType());
        if (definitions == null) {
            definitions = new ArrayList<>();
        }
        definitions.add(definition);
        definitionsByType.put(definition.getType(), definitions);
    }

    @Override
    public void addAllDefinitions(final Collection<? extends TeaLeaf.Definition> definitions) {
        for (final TeaLeaf.Definition definition : definitions) {
            addDefinition(definition);
        }
    }
}
