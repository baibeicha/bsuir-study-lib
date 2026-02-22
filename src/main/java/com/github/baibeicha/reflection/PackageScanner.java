package com.github.baibeicha.reflection;

import com.github.baibeicha.reflection.util.AnnotationUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageScanner {

    public static boolean PRINT_COULD_NOT_LOAD_CLASS = false;

    public Set<Class<?>> findAllClasses(String packageName) {
        String packagePath = packageName.replace('.', '/');
        URL resource = getClass().getClassLoader().getResource(packagePath);

        if (resource == null) {
            System.err.println("Resource not found for package: " + packageName);
            return Collections.emptySet();
        }

        try {
            URI uri = resource.toURI();
            String protocol = uri.getScheme();

            if ("file".equalsIgnoreCase(protocol)) {
                return findClassesInDirectory(Paths.get(uri), packageName);
            } else if ("jar".equalsIgnoreCase(protocol)) {
                return findClassesInJar(uri, packagePath);
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace(System.err);
        }
        return Collections.emptySet();
    }

    private Set<Class<?>> findClassesInDirectory(Path directory, String packageName) throws IOException {
        if (!Files.exists(directory)) {
            return Collections.emptySet();
        }

        try (Stream<Path> walk = Files.walk(directory)) {
            return walk
                    .filter(path -> path.toString().endsWith(".class"))
                    .map(path -> {
                        String className = packageName + "." + path.getFileName().toString().replace(".class", "");
                        return loadClass(className);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }

    private Set<Class<?>> findClassesInJar(URI jarUri, String packagePath) throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        try (FileSystem fileSystem = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
            Path packageDirPath = fileSystem.getPath(packagePath);
            try (Stream<Path> walk = Files.walk(packageDirPath)) {
                walk
                        .filter(path -> path.toString().endsWith(".class"))
                        .forEach(path -> {
                            String fullPath = path.toString().replace('/', '.');
                            String className = fullPath.substring(0, fullPath.length() - ".class".length());
                            Class<?> loadedClass = loadClass(className);
                            if (loadedClass != null) {
                                classes.add(loadedClass);
                            }
                        });
            }
        }
        return classes;
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
             if (PRINT_COULD_NOT_LOAD_CLASS) {
                 System.err.println("Could not load class: " + className);
             }
            return null;
        }
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