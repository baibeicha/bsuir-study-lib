package com.github.baibeicha.ioc.context.registry;


import com.github.baibeicha.ioc.leaf.TeaLeaf;

import java.util.Collection;
import java.util.List;

public interface TeaLeafDefinitionRegistry {
    TeaLeaf.Definition getDefinition(String name);

    List<TeaLeaf.Definition> getDefinition(Class<?> type);

    void addDefinition(TeaLeaf.Definition definition);

    void addAllDefinitions(Collection<? extends TeaLeaf.Definition> definitions);
}
