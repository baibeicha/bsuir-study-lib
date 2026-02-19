package com.github.baibeicha.test;

import com.github.baibeicha.collections.Pair;
import com.github.baibeicha.reflection.PackageScanner;
import com.github.baibeicha.test.annotation.Test;
import com.github.baibeicha.test.annotation.TestConfiguration;
import com.github.baibeicha.test.annotation.TestTarget;
import com.github.baibeicha.test.exception.TestConfigurationAnnotationNotFoundException;
import com.github.baibeicha.test.exception.TestFailedException;
import com.github.baibeicha.test.exception.TestMethodHasTooManyParameters;
import com.github.baibeicha.test.exception.UnableInvokeTestMethodException;
import com.github.baibeicha.test.result.TestResultPrinter;
import com.github.baibeicha.util.ArrayUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * USAGE EXAMPLES
 * <p>
 * TestEngine testEngine1 = new TestEngine(Main.class); - needs @TestConfiguration annotation<br>
 * testEngine1.test();
 * <p>
 * TestEngine.withDefaults(Main.class).test(); - Make ContextConfiguration with Main.class package
 * <p>
 * TestEngine.ContextConfiguration config = new TestEngine.ContextConfiguration.Builder()<br>
 *      .add(Main.class)<br>
 *      .add("org.baibei.test.tests")<br>
 *      .build();<br>
 * TestEngine testEngine2 = new TestEngine(config);<br>
 * testEngine2.test();
 * <p>
 * TestEngine testEngine3 = new TestEngine(<br>
 *      new TestEngine.ContextConfiguration(builder -> builder<br>
 *          .add(Main.class)<br>
 *          .add("org.baibei.test.tests")
 * ));<br>
 * testEngine3.test();
 * <p>
 * TestEngine testEngine4 = new TestEngine(builder -> builder<br>
 *      .add(Main.class, TimeParserTest.class)<br>
 *      .add("org.baibei.test.tests")<br>
 *      .packageScanner(new PackageScanner())<br>
 * );<br>
 * testEngine4.test();<p>
 */
public class TestEngine {

    public static boolean PRINT_TEST_RESULT = true;
    public static String TARGET_METHOD_SEPARATOR = ":";

    private final Class<?> configClass;
    private final Map<Class<?>, String> testTargets;
    private final Map<String, Method> testMethods;
    private final Map<Class<?>, Set<Pair<String, Method>>> testData;
    private final PackageScanner packageScanner;

    private Map<String, Boolean> lastTestResult = null;

    private Configuration config = null;

    public TestEngine(Function<Configuration.Builder, Configuration.Builder> function) {
        Configuration config = function.apply(new Configuration.Builder()).build();
        this.configClass = config.getClass();
        this.testTargets = new HashMap<>();
        this.testMethods = new HashMap<>();
        this.testData = new HashMap<>();
        this.packageScanner =
                config.getPackageScanner() == null ? new PackageScanner() : config.getPackageScanner();
        this.config = config;

        init(ConfigurationType.CONFIGURATION_CLASS);
    }

    public TestEngine(Configuration config) {
        this.configClass = config.getClass();
        this.testTargets = new HashMap<>();
        this.testMethods = new HashMap<>();
        this.testData = new HashMap<>();
        this.packageScanner =
                config.getPackageScanner() == null ? new PackageScanner() : config.getPackageScanner();
        this.config = config;

        init(ConfigurationType.CONFIGURATION_CLASS);
    }

    public TestEngine(Class<?> configClass, PackageScanner packageScanner) {
        this.configClass = configClass;
        this.testTargets = new HashMap<>();
        this.testMethods = new HashMap<>();
        this.testData = new HashMap<>();
        this.packageScanner = packageScanner;

        init(ConfigurationType.ANNOTATION);
    }

    public TestEngine(Class<?> configClass) {
        this.configClass = configClass;
        this.testTargets = new HashMap<>();
        this.testMethods = new HashMap<>();
        this.testData = new HashMap<>();
        this.packageScanner = new PackageScanner();

        init(ConfigurationType.ANNOTATION);
    }

    public static TestEngine withDefaults(Class<?> configClass) {
        return new TestEngine(builder -> builder
                .add(configClass.getPackageName())
        );
    }

    protected enum ConfigurationType {
        ANNOTATION,
        CONFIGURATION_CLASS
    }

    protected void init(ConfigurationType configType) {
        findTestTargets(configType);
        findTestMethods();
    }

    public void test() {
        test(PRINT_TEST_RESULT);
    }

    public void test(boolean print) {
        var result = startTest();
        lastTestResult = result;

        if (checkTestResult(result, print)) {
            throw new TestFailedException("Test failed");
        }
    }

    public Map<String, Boolean> testWithResults() {
        return testWithResults(PRINT_TEST_RESULT);
    }

    public Map<String, Boolean> testWithResults(boolean print) {
        var result = startTest();
        lastTestResult = result;

        if (checkTestResult(result, print)) {
            throw new TestFailedException("Test isn't passed");
        }

        return result;
    }

    public Map<String, Boolean> startTest() {
        final Map<String, Boolean> result = new HashMap<>();

        for (Class<?> target : testData.keySet()) {
            for (Pair<String, Method> pair : testData.get(target)) {
                Method method = pair.getValue();

                try {
                    int parametersCount = checkMethod(method);
                    if (parametersCount != 0) {
                        throw new TestMethodHasTooManyParameters(
                                "Method " + method.getName() + " has " + parametersCount +
                                        " parameter" + (parametersCount % 10 == 1 ? "" : "s") +
                                        ", but must have 0"
                        );
                    }

                    Object ret = method.invoke(target.getDeclaredConstructor().newInstance());

                    result.put(pair.getKey(), ret instanceof Boolean ? (Boolean) ret : Boolean.TRUE);
                } catch (IllegalAccessException e) {
                    throw new UnableInvokeTestMethodException(e);
                } catch (TestMethodHasTooManyParameters e) {
                    throw e;
                } catch (AssertionError | TestFailedException e) {
                    result.put(pair.getKey(), false);
                } catch (Exception other) {
                    result.put(pair.getKey(), false);
                    other.getCause().printStackTrace(System.err);
                }
            }
        }

        return result;
    }

    public boolean checkTestResult(Map<String, Boolean> testResult, boolean print) {
        if (print) {
            TestResultPrinter.print(testResult);
        }

        for (Map.Entry<String, Boolean> entry : testResult.entrySet()) {
            if (!entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    protected int checkMethod(Method method) {
        method.setAccessible(true);
        return method.getParameterCount();
    }

    protected void findTestTargets(ConfigurationType configType) {
        List<Class<?>> foundClasses = switch (configType) {
            case ANNOTATION -> findClasses();
            case CONFIGURATION_CLASS -> findClasses(config);
        };

        for (Class<?> foundClass : foundClasses) {
            if (foundClass.isAnnotationPresent(TestTarget.class)) {
                TestTarget testTarget = foundClass.getAnnotation(TestTarget.class);
                String testName = testTarget.value().isEmpty() ? foundClass.getSimpleName() : testTarget.value();
                testTargets.put(foundClass, testName);
                testData.put(foundClass, new HashSet<>());
            }
        }
    }

    protected List<Class<?>> findClasses() {
        TestConfiguration testConfig = configClass.getAnnotation(TestConfiguration.class);
        if (testConfig == null) {
            throw new TestConfigurationAnnotationNotFoundException(
                    "Not found @TestConfiguration annotation in " + configClass.getName()
            );
        }

        List<Class<?>> result = new ArrayList<>();

        Collections.addAll(result, testConfig.classes());

        String[] packages = testConfig.packages();
        for (String pkgPath : packages) {
            result.addAll(packageScanner.findAllClasses(pkgPath));
        }

        if (result.isEmpty()) {
            result.add(configClass);
        }

        return result;
    }

    protected List<Class<?>> findClasses(Configuration config) {
        List<Class<?>> result = new ArrayList<>(config.getClasses());

        List<String> packages = config.getPackages();
        for (String pkgPath : packages) {
            result.addAll(packageScanner.findAllClasses(pkgPath));
        }

        return result;
    }

    protected void findTestMethods() {
        for (Class<?> target : testTargets.keySet()) {
            List<Method> methods = ArrayUtils.concatToList(target.getMethods(), target.getDeclaredMethods());
            Set<Pair<String, Method>> targetTestMethods = testData.get(target);

            for (Method method : methods) {
                if (method.isAnnotationPresent(Test.class)) {
                    Test test = method.getAnnotation(Test.class);
                    String testName = testTargets.get(target) + TARGET_METHOD_SEPARATOR +
                            (test.value().isEmpty() ? method.getName() : test.value());
                    testMethods.put(testName, method);
                    targetTestMethods.add(new Pair<>(testName, method));
                }
            }
        }
    }

    public Map<Class<?>, String> getTestTargets() {
        return testTargets;
    }

    public Map<String, Method> getTestMethods() {
        return testMethods;
    }

    public Map<Class<?>, Set<Pair<String, Method>>> getTestData() {
        return testData;
    }

    public Map<String, Boolean> getLastTestResult() {
        return lastTestResult;
    }

    public static final class Configuration {

        private final List<String> packages;
        private final List<Class<?>> classes;

        private PackageScanner packageScanner = null;

        public Configuration(List<String> packages, List<Class<?>> classes) {
            this.packages = packages;
            this.classes = classes;
        }

        public Configuration(List<String> packages, List<Class<?>> classes, PackageScanner packageScanner) {
            this.packages = packages;
            this.classes = classes;
            this.packageScanner = packageScanner;
        }

        public Configuration(Function<Builder, Builder> function) {
            Configuration configuration = function.apply(new Builder()).build();
            packages = configuration.getPackages();
            classes = configuration.getClasses();
            packageScanner = configuration.getPackageScanner();
        }

        public List<String> getPackages() {
            return packages;
        }

        public List<Class<?>> getClasses() {
            return classes;
        }

        public PackageScanner getPackageScanner() {
            return packageScanner;
        }

        public static class Builder {

            private final List<String> packages;
            private final List<Class<?>> classes;

            private PackageScanner packageScanner = null;

            public Builder() {
                this.packages = new ArrayList<>();
                this.classes = new ArrayList<>();
            }

            public Builder add(String... packages) {
                this.packages.addAll(Arrays.asList(packages));
                return this;
            }

            public Builder add(Class<?>... classes) {
                this.classes.addAll(Arrays.asList(classes));
                return this;
            }

            public Builder packageScanner(PackageScanner packageScanner) {
                this.packageScanner = packageScanner;
                return this;
            }

            public Configuration build() {
                return new Configuration(packages, classes, packageScanner);
            }
        }
    }
}
