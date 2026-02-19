package com.github.baibeicha.ioc;

import com.github.baibeicha.ioc.annotation.configuration.Configuration;
import com.github.baibeicha.ioc.annotation.configuration.PackageScan;
import com.github.baibeicha.ioc.annotation.configuration.TeaApplicationConfiguration;
import com.github.baibeicha.ioc.application.event.AppEvent;
import com.github.baibeicha.ioc.application.event.EventType;
import com.github.baibeicha.ioc.application.event.context.EventContext;
import com.github.baibeicha.ioc.application.event.listener.AppEventListener;
import com.github.baibeicha.ioc.context.TeaApplicationContext;
import com.github.baibeicha.ioc.reflection.ClassDependenciesScanner;
import com.github.baibeicha.reflection.PackageScanner;
import com.github.baibeicha.reflection.util.AnnotationUtils;
import com.github.baibeicha.test.TestEngine;
import com.github.baibeicha.test.annotation.TestConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TeaApplication {

    protected final Class<?> configClass;
    protected final TeaApplicationContext context;
    protected final EventContext eventContext;
    protected final PackageScanner packageScanner;
    protected final TestEngine testEngine;
    protected final ContextConfiguration config;
    protected final ClassDependenciesScanner classDependenciesScanner;

    protected boolean startTest = true;
    protected boolean printTestResult = false;

    public TeaApplication() {
        this.configClass = TeaApplicationContext.class;
        this.packageScanner = new PackageScanner();
        this.classDependenciesScanner = new ClassDependenciesScanner();

        config = new ContextConfiguration();
        scanPackages();

        this.eventContext = new EventContext(configClass, packageScanner, config);
        this.context = new TeaApplicationContext(
                configClass, packageScanner, config, classDependenciesScanner, eventContext
        );
        this.testEngine = TestEngine.withDefaults(configClass);
    }

    public TeaApplication(Class<?> configClass) {
        this.configClass = configClass;
        this.packageScanner = new PackageScanner();
        this.classDependenciesScanner = new ClassDependenciesScanner();

        config = new ContextConfiguration();
        scanPackages();

        this.eventContext = new EventContext(configClass, packageScanner, config);
        this.context = new TeaApplicationContext(
                configClass, packageScanner, config, classDependenciesScanner, eventContext
        );

        if (AnnotationUtils.isAnnotated(configClass, TestConfiguration.class)) {
            this.testEngine = new TestEngine(configClass);
        } else {
            this.testEngine = TestEngine.withDefaults(configClass);
        }
    }

    public TeaApplication(Class<?> configClass, Class<?> testConfigClass) {
        this.configClass = configClass;
        this.packageScanner = new PackageScanner();
        this.classDependenciesScanner = new ClassDependenciesScanner();

        config = new ContextConfiguration();
        scanPackages();

        this.eventContext = new EventContext(configClass, packageScanner, config);
        this.context = new TeaApplicationContext(
                configClass, packageScanner, config, classDependenciesScanner, eventContext
        );

        this.testEngine = new TestEngine(testConfigClass);
    }

    public TeaApplication(Class<?> configClass, TestEngine.Configuration testConfig) {
        this.configClass = configClass;
        this.packageScanner = new PackageScanner();
        this.classDependenciesScanner = new ClassDependenciesScanner();

        config = new ContextConfiguration();
        scanPackages();

        this.eventContext = new EventContext(configClass, packageScanner, config);
        this.context = new TeaApplicationContext(
                configClass, packageScanner, config, classDependenciesScanner, eventContext
        );

        this.testEngine = new TestEngine(testConfig);
    }

    public TeaApplication(Class<?> configClass, TestEngine testEngine) {
        this.configClass = configClass;
        this.packageScanner = new PackageScanner();
        this.classDependenciesScanner = new ClassDependenciesScanner();

        config = new ContextConfiguration();
        scanPackages();

        this.eventContext = new EventContext(configClass, packageScanner, config);
        this.context = new TeaApplicationContext(
                configClass, packageScanner, config, classDependenciesScanner, eventContext
        );

        this.testEngine = testEngine;
    }

    public static TeaApplication run(Class<?> configClass) {
        TeaApplication application = new TeaApplication(configClass);
        application.run();
        return application;
    }

    public static TeaApplication run(Class<?> configClass, boolean startTest, boolean printTestResult) {
        TeaApplication application = new TeaApplication(configClass);
        application.startTest = startTest;
        application.printTestResult = printTestResult;
        application.run();
        return application;
    }

    public static TeaApplication run(Class<?> configClass, Class<?> testConfigClass) {
        TeaApplication application = new TeaApplication(configClass, testConfigClass);
        application.run();
        return application;
    }

    public static TeaApplication run(Class<?> configClass, TestEngine.Configuration testConfig) {
        TeaApplication application = new TeaApplication(configClass, testConfig);
        application.run();
        return application;
    }

    public static TeaApplication run(Class<?> configClass, TestEngine testEngine) {
        TeaApplication application = new TeaApplication(configClass, testEngine);
        application.run();
        return application;
    }

    public TeaApplicationContext run() {
        if (startTest) {
            testEngine.test(printTestResult);
        }

        eventContext.start();

        eventContext.findEvents(EventType.APPLICATION_STARTING)
                        .forEach(eventContext::registerEvent);

        context.start();

        eventContext.findEvents(EventType.APPLICATION_RUNNING)
                .forEach(eventContext::registerEvent);

        return context;
    }

    public void shutdown() {
        context.close();

        eventContext.findEvents(EventType.APPLICATION_STOPPING)
                        .forEach(eventContext::registerEvent);

        eventContext.clear();
    }

    public void registerEvent(AppEvent event) {
        eventContext.registerEvent(event);
    }

    public void registerListener(AppEventListener listener) {
        eventContext.registerListener(listener);
    }

    public <Type> Type registerTeaLeaf(Class<Type> clazz) {
        String name = clazz.getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return registerTeaLeaf(clazz, name);
    }

    public <Type> Type registerTeaLeaf(Class<Type> clazz, String name) {
        return context.registerTeaLeaf(clazz, name);
    }

    public Object getTeaLeaf(String name) {
        return context.getTeaLeaf(name);
    }

    public <Type> Type getTeaLeaf(Class<Type> type) {
        return context.getTeaLeaf(type);
    }

    public <Type> Type getTeaLeaf(Class<Type> type, String name) {
        return context.getTeaLeaf(type, name);
    }

    public <Type> List<Type> getTeaLeafs(Class<Type> type) {
        return context.getTeaLeafs(type);
    }

    protected void scanPackages() {
        addConfigClassPackages();
        addFoundConfigsPackages();
    }

    protected void addConfigClassPackages() {
        TeaApplicationConfiguration applicationConfiguration =
                configClass.getAnnotation(TeaApplicationConfiguration.class);
        String[] configClassPackages = applicationConfiguration.packages();

        Collections.addAll(config.packages, configClassPackages);
        config.packages.add(configClass.getPackage().getName());
    }

    protected void addFoundConfigsPackages() {
        Set<PackageScan> packageScanConfigs = new HashSet<>();
        for (String pack : config.packages) {
            packageScanConfigs.addAll(scanPackageForConfigurations(pack));
        }

        Set<String> packages = new HashSet<>();
        for (PackageScan packageScan : packageScanConfigs) {
            if (packageScan != null && packageScan.value() != null) {
                packages.addAll(List.of(packageScan.value()));
            }
        }

        config.packages.addAll(packages);
    }

    protected Set<PackageScan> scanPackageForConfigurations(String packageName) {
        return new ArrayList<>(packageScanner.findAllClassesWithAnnotation(packageName, Configuration.class))
                .stream()
                .map(clazz -> clazz.getAnnotation(PackageScan.class))
                .collect(Collectors.toSet());
    }

    public static class ContextConfiguration {
        public final Set<String> packages = new HashSet<>();
    }

    public TestEngine getTestEngine() {
        return testEngine;
    }

    public void setStartTest(boolean test) {
        startTest = test;
    }

    public boolean isStartTest() {
        return startTest;
    }

    public void setPrintTestResult(boolean print) {
        printTestResult = print;
    }

    public boolean isPrintTestResult() {
        return printTestResult;
    }
}
