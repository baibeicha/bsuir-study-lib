package com.github.baibeicha.util;

import java.util.List;

public class PrintCycle {

    public static String buildCycleMessage(List<String> dependencyStack) {
        if (dependencyStack == null || dependencyStack.isEmpty()) {
            return "No circular dependency detected";
        }

        String currentBean = dependencyStack.getLast();
        int startIndex = dependencyStack.subList(0, dependencyStack.size() - 1).indexOf(currentBean);

        if (startIndex == -1) {
            return "Cycle detected: " + currentBean;
        }

        List<String> cycle = dependencyStack.subList(startIndex, dependencyStack.size());

        return cycle.size() <= 5
                ? formatCompactCycle(cycle)
                : formatLinearCycle(cycle);
    }

    private static String formatCompactCycle(List<String> cycle) {
        int maxWidth = cycle.stream()
                .mapToInt(String::length)
                .max()
                .orElse(20) + 4;

        StringBuilder sb = new StringBuilder();
        sb.append("\nCircular dependency detected:\n");

        sb.append("+").append(repeatChar('-', maxWidth)).append("+\n");

        for (int i = 0; i < cycle.size(); i++) {
            String beanLine = String.format("| %-" + maxWidth + "s |", centerString(cycle.get(i), maxWidth - 2));
            sb.append(beanLine);

            sb.append("\n");

            if (i < cycle.size() - 1) {
                String arrowLine = String.format("| %-" + maxWidth + "s |", centerString("\\/", maxWidth - 2));
                sb.append(arrowLine).append("\n");
            }
        }

        String closureArrow = String.format("| %-" + maxWidth + "s |", centerString("/\\", maxWidth - 2));
        sb.append(closureArrow).append("\n");

        sb.append("+").append(repeatChar('-', maxWidth)).append("+");

        return sb.toString();
    }

    private static String formatLinearCycle(List<String> cycle) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nCircular dependency detected:\n");

        sb.append(cycle.getFirst()).append("\n");
        for (int i = 1; i < cycle.size(); i++) {
            sb.append("    -> ").append(cycle.get(i)).append("\n");
        }
        sb.append("    \\/ cycle back to ").append(cycle.getFirst());

        return sb.toString();
    }

    private static String centerString(String s, int width) {
        if (s.length() >= width) return s;
        int padding = width - s.length();
        int leftPadding = padding / 2;
        int rightPadding = padding - leftPadding;
        return repeatChar(' ', leftPadding) + s + repeatChar(' ', rightPadding);
    }

    private static String repeatChar(char c, int count) {
        if (count <= 0) return "";
        return new String(new char[count]).replace('\0', c);
    }
}
