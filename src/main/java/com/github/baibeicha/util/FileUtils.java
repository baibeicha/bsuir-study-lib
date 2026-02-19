package com.github.baibeicha.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

    public static boolean createFile(String path) {
        File file = new File(path);
        return createFile(file);
    }

    public static boolean createFile(File file) {
        try {
            if (!file.exists()) {
                return file.createNewFile();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Can't create file: " + e.getMessage());
            return false;
        }
    }

    public static void writeFile(String path, boolean append, String... content) {
        try (FileWriter writer = new FileWriter(path, append)) {
            for (String s : content) {
                writer.write(s + "\n");
            }
            writer.flush();
        } catch (Exception e) {
            System.err.println("Can't write file: " + e.getMessage());
        }
    }

    public static List<String> readFile(String path) {
        List<String> list = new ArrayList<>();

        try (Scanner sc = new Scanner(new File(path))) {
            while (sc.hasNextLine()) {
                list.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }

        return list;
    }

    public static String readFileAsString(String path) {
        List<String> list = readFile(path);

        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append("\n");
        }

        return sb.toString();
    }

    public static void clearFile(String path) {
        writeFile(path, false, "");
    }

    public static String getUser() {
        return System.getProperty("user.name");
    }

    public static boolean createDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    public static boolean delete(String path) {
        File file = new File(path);
        try {
            return file.delete();
        } catch (Exception e) {
            System.err.println("Can't delete file: " + e.getMessage());
            return false;
        }
    }
}
