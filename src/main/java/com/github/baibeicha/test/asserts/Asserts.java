package com.github.baibeicha.test.asserts;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Asserts {

    public static void assertTrue(boolean b) {
        if (!b) {
            throw new AssertionError();
        }
    }

    public static void assertFalse(boolean b) {
        if (b) {
            throw new AssertionError();
        }
    }

    public static void assertNotNull(Object o) {
        if (o == null) {
            throw new AssertionError();
        }
    }

    public static void assertNull(Object o) {
        if (o != null) {
            throw new AssertionError();
        }
    }

    public static <Type> void assertEquals(Type arg, Type value) {
        if (!arg.equals(value)) {
            throw new AssertionError();
        }
    }

    public static <Type> void assertNotEquals(Type arg, Type value) {
        if (arg.equals(value)) {
            throw new AssertionError();
        }
    }

    public static void assertSameObject(Object arg, Object expected) {
        if (arg != expected) {
            throw new AssertionError();
        }
    }

    public static void assertNotSameObject(Object arg, Object expected) {
        if (arg == expected) {
            throw new AssertionError();
        }
    }

    public static void assertLength(Object[] array, int length) {
        if (array.length != length) {
            throw new AssertionError();
        }
    }

    public static void assertLength(Collection<?> c, int length) {
        if (c.size() != length) {
            throw new AssertionError();
        }
    }

    public static void assertEquals(Object[] array, Object[] expected) {
        if (array.length != expected.length) {
            throw new AssertionError();
        }

        for (int i = 0; i < array.length; i++) {
            if (!array[i].equals(expected[i])) {
                throw new AssertionError();
            }
        }
    }

    public static void assertEquals(List<?> list, Object[] expected) {
        if (list.size() != expected.length) {
            throw new AssertionError();
        }

        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(expected[i])) {
                throw new AssertionError();
            }
        }
    }

    public static void assertEquals(Object[] array, List<?> expected) {
        if (array.length != expected.size()) {
            throw new AssertionError();
        }

        for (int i = 0; i < array.length; i++) {
            if (!array[i].equals(expected.get(i))) {
                throw new AssertionError();
            }
        }
    }

    public static void assertEquals(List<?> list, List<?> expected) {
        if (list.size() != expected.size()) {
            throw new AssertionError();
        }

        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(expected.get(i))) {
                throw new AssertionError();
            }
        }
    }

    public static void assertNotEquals(Object[] array, Object[] expected) {
        if (array.length != expected.length) {
            return;
        }

        for (int i = 0; i < array.length; i++) {
            if (!array[i].equals(expected[i])) {
                return;
            }
        }

        throw new AssertionError();
    }

    public static void assertNotEquals(List<?> list, List<?> expected) {
        if (list.size() != expected.size()) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(expected.get(i))) {
                return;
            }
        }

        throw new AssertionError();
    }

    public static void assertNotEquals(Object[] array, List<?> expected) {
        if (array.length != expected.size()) {
            return;
        }

        for (int i = 0; i < array.length; i++) {
            if (!array[i].equals(expected.get(i))) {
                return;
            }
        }

        throw new AssertionError();
    }

    public static void assertNotEquals(List<?> list, Object[] expected) {
        if (list.size() != expected.length) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(expected[i])) {
                return;
            }
        }

        throw new AssertionError();
    }

    public static void assertContains(Object[] array, Object... expected) {
        if (array.length < expected.length) {
            throw new AssertionError();
        }

        int contains = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(expected[i])) {
                contains++;
            }
        }

        if (contains != expected.length) {
            throw new AssertionError();
        }
    }

    public static void assertContains(List<?> list, Object... expected) {
        if (list.size() < expected.length) {
            throw new AssertionError();
        }

        int contains = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.contains(expected[i])) {
                contains++;
            }
        }

        if (contains != expected.length) {
            throw new AssertionError();
        }
    }

    public static void assertContains(Object[] array, List<?> expected) {
        if (array.length < expected.size()) {
            throw new AssertionError();
        }

        int contains = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(expected.get(i))) {
                contains++;
            }
        }

        if (contains != expected.size()) {
            throw new AssertionError();
        }
    }

    public static <Type> void assertContains(Collection<Type> list, Collection<Type> expected) {
        if (!list.containsAll(expected)) {
            throw new AssertionError();
        }
    }

    public static <T, R> R assertNoException(Function<T, R> function, T input) {
        try {
            return function.apply(input);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void assertNoException(Runnable function) {
        try {
            function.run();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void assertException(Runnable function) {
        try {
            function.run();
            throw new AssertionError();
        } catch (Exception ignored) {
        }
    }
}
