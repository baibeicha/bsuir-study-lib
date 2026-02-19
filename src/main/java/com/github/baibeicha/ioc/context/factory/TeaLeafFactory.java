package com.github.baibeicha.ioc.context.factory;

import com.github.baibeicha.ioc.leaf.TeaLeaf;

import java.util.List;
import java.util.Map;

public interface TeaLeafFactory {

    TeaLeaf createTeaLeaf(TeaLeaf.Definition definition);

    TeaLeaf register(TeaLeaf leaf);

    Object getTeaLeaf(String name);

    <Type> Type getTeaLeaf(Class<Type> type);

    <Type> Type getTeaLeaf(Class<Type> type, String name);

    <Type> List<Type> getTeaLeafs(Class<Type> type);

    void clear();

    Map<String, TeaLeaf> getTeaLeafs();
}
