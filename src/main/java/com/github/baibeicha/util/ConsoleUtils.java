package com.github.baibeicha.util;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleUtils {

    private final static Scanner SCANNER = new Scanner(System.in);

    public static void clear() {
        try {
            if (OSDetector.isWindows()) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else if (OSDetector.isLinux()) {
                new ProcessBuilder("/bin/bash", "-c", "clear").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (InterruptedException | IOException e) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    public static void waitEnter() {
        System.out.println("Нажмите Enter, чтобы продолжить...");
        SCANNER.nextLine();
    }

    public static Scanner getScannerInstance() {
        return SCANNER;
    }
}
