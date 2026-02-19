package com.github.baibeicha.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ArrayUtils {

    public static int[] toArray(Integer... nums) {
        int[] ints = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            ints[i] = nums[i];
        }
        return ints;
    }

    public static Integer[] toArray(int... nums) {
        Integer[] ints = new Integer[nums.length];
        for (int i = 0; i < nums.length; i++) {
            ints[i] = nums[i];
        }
        return ints;
    }

    public static long[] toArray(Long... nums) {
        long[] longs = new long[nums.length];
        for (int i = 0; i < nums.length; i++) {
            longs[i] = nums[i];
        }
        return longs;
    }

    public static Long[] toArray(long... nums) {
        Long[] longs = new Long[nums.length];
        for (int i = 0; i < nums.length; i++) {
            longs[i] = nums[i];
        }
        return longs;
    }

    public static double[] toArray(Double... nums) {
        double[] doubles = new double[nums.length];
        for (int i = 0; i < nums.length; i++) {
            doubles[i] = nums[i];
        }
        return doubles;
    }

    public static Double[] toArray(double... nums) {
        Double[] doubles = new Double[nums.length];
        for (int i = 0; i < nums.length; i++) {
            doubles[i] = nums[i];
        }
        return doubles;
    }

    public static float[] toArray(Float... nums) {
        float[] floats = new float[nums.length];
        for (int i = 0; i < nums.length; i++) {
            floats[i] = nums[i];
        }
        return floats;
    }

    public static Float[] toArray(float... nums) {
        Float[] floats = new Float[nums.length];
        for (int i = 0; i < nums.length; i++) {
            floats[i] = nums[i];
        }
        return floats;
    }

    public static short[] toArray(Short... nums) {
        short[] shorts = new short[nums.length];
        for (int i = 0; i < nums.length; i++) {
            shorts[i] = nums[i];
        }
        return shorts;
    }

    public static Short[] toArray(short... nums) {
        Short[] shorts = new Short[nums.length];
        for (int i = 0; i < nums.length; i++) {
            shorts[i] = nums[i];
        }
        return shorts;
    }

    public static byte[] toArray(Byte... nums) {
        byte[] bytes = new byte[nums.length];
        for (int i = 0; i < nums.length; i++) {
            bytes[i] = nums[i];
        }
        return bytes;
    }

    public static Byte[] toArray(byte... nums) {
        Byte[] bytes = new Byte[nums.length];
        for (int i = 0; i < nums.length; i++) {
            bytes[i] = nums[i];
        }
        return bytes;
    }

    public static boolean[] toArray(Boolean... bools) {
        boolean[] booleans = new boolean[bools.length];
        for (int i = 0; i < bools.length; i++) {
            booleans[i] = bools[i];
        }
        return booleans;
    }

    public static Boolean[] toArray(boolean... bools) {
        Boolean[] booleans = new Boolean[bools.length];
        for (int i = 0; i < bools.length; i++) {
            booleans[i] = bools[i];
        }
        return booleans;
    }

    public static char[] toArray(Character... chars) {
        char[] characters = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            characters[i] = chars[i];
        }
        return characters;
    }

    public static Character[] toArray(char... chars) {
        Character[] characters = new Character[chars.length];
        for (int i = 0; i < chars.length; i++) {
            characters[i] = chars[i];
        }
        return characters;
    }

    public static <Type> Type[] toArray(List<Type> values, Class<Type> clazz) {
        Type[] types = (Type[]) Array.newInstance(clazz, values.size());
        for (int i = 0; i < values.size(); i++) {
            types[i] = values.get(i);
        }
        return types;
    }

    public static ArrayList<Integer> toArrayList(int... nums) {
        ArrayList<Integer> ints = new ArrayList<>(nums.length);
        for (int i : nums) {
            ints.add(i);
        }
        return ints;
    }

    public static LinkedList<Integer> toLinkedList(int... nums) {
        LinkedList<Integer> ints = new LinkedList<>();
        for (int i : nums) {
            ints.add(i);
        }
        return ints;
    }

    public static ArrayList<Long> toArrayList(long... nums) {
        ArrayList<Long> longs = new ArrayList<>(nums.length);
        for (long l : nums) {
            longs.add(l);
        }
        return longs;
    }

    public static LinkedList<Long> toLinkedList(long... nums) {
        LinkedList<Long> longs = new LinkedList<>();
        for (long l : nums) {
            longs.add(l);
        }
        return longs;
    }

    public static ArrayList<Double> toArrayList(double... nums) {
        ArrayList<Double> doubles = new ArrayList<>(nums.length);
        for (double d : nums) {
            doubles.add(d);
        }
        return doubles;
    }

    public static LinkedList<Double> toLinkedList(double... nums) {
        LinkedList<Double> doubles = new LinkedList<>();
        for (double d : nums) {
            doubles.add(d);
        }
        return doubles;
    }

    public static ArrayList<Float> toArrayList(float... nums) {
        ArrayList<Float> floats = new ArrayList<>(nums.length);
        for (float f : nums) {
            floats.add(f);
        }
        return floats;
    }

    public static LinkedList<Float> toLinkedList(float... nums) {
        LinkedList<Float> floats = new LinkedList<>();
        for (float f : nums) {
            floats.add(f);
        }
        return floats;
    }

    public static ArrayList<Short> toArrayList(short... nums) {
        ArrayList<Short> shorts = new ArrayList<>(nums.length);
        for (short s : nums) {
            shorts.add(s);
        }
        return shorts;
    }

    public static LinkedList<Short> toLinkedList(short... nums) {
        LinkedList<Short> shorts = new LinkedList<>();
        for (short s : nums) {
            shorts.add(s);
        }
        return shorts;
    }

    public static ArrayList<Byte> toArrayList(byte... nums) {
        ArrayList<Byte> bytes = new ArrayList<>(nums.length);
        for (byte b : nums) {
            bytes.add(b);
        }
        return bytes;
    }

    public static LinkedList<Byte> toLinkedList(byte... nums) {
        LinkedList<Byte> bytes = new LinkedList<>();
        for (byte b : nums) {
            bytes.add(b);
        }
        return bytes;
    }

    public static ArrayList<Boolean> toArrayList(boolean... nums) {
        ArrayList<Boolean> booleans = new ArrayList<>(nums.length);
        for (boolean b : nums) {
            booleans.add(b);
        }
        return booleans;
    }

    @SafeVarargs
    public static <Type> Type[] concat(Type[]... arrays) {
        int length = 0;
        for (Type[] array : arrays) {
            length += array.length;
        }

        Type[] types = (Type[]) Array.newInstance(arrays[0].getClass(), length);
        int index = 0;
        for (Type[] array : arrays) {
            System.arraycopy(array, 0, types, index, array.length);
            index += array.length;
        }

        return types;
    }

    public static <Type> List<Type> concatToList(Type[]... arrays) {
        int length = 0;
        for (Type[] array : arrays) {
            length += array.length;
        }

        List<Type> types = new ArrayList<>(length);
        for (Type[] array : arrays) {
            Collections.addAll(types, array);
        }

        return types;
    }
}
