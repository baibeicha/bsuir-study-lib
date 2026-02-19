package com.github.baibeicha.reflection;

import com.github.baibeicha.reflection.util.AnnotationUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PackageScanner {

    public Set<Class<?>> findAllClasses(String packageName) {
        String path = packageName.replace('.', '/');
        URL resource = getClass().getClassLoader().getResource(path);

        if (resource == null) {
            return Collections.emptySet();
        }

        File directory = new File(resource.getFile());
        if (!directory.exists()) {
            return Collections.emptySet();
        }

        return findClasses(directory, packageName);
    }

    public Set<Class<?>> findAllClassesWithAnnotation(String packageName,
                                                      Class<? extends Annotation> annotation) {
        return findAllClasses(packageName).stream()
                .filter(clazz -> AnnotationUtils.isAnnotated(clazz, annotation))
                .collect(Collectors.toSet());
    }

    private Set<Class<?>> findClasses(File directory, String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        File[] files = directory.listFiles();

        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found: " + className);
                }
            }
        }
        return classes;
    }
}