package com.github.baibeicha.test.result;

import com.github.baibeicha.util.StringUtils;

import java.util.Map;

public class TestResultPrinter {

    public static int TAB_AMOUNT = 2;
    public static int TAB_WIDTH = 4;

    public static void print(Map<String, Boolean> testResult) {
        long maxNameLength = maxLength(testResult);
        for (Map.Entry<String, Boolean> entry : testResult.entrySet()) {
            System.out.println(entry.getKey()
                    + StringUtils.multiply("\t", (int) Math.ceil(
                            (maxNameLength - entry.getKey().length()) / (double) TAB_WIDTH) + TAB_AMOUNT)
                    + entry.getValue());
        }
    }

    private static long maxLength(Map<String, Boolean> testResult) {
        long maxNameLength = 0;
        for (String name : testResult.keySet()) {
            if (name.length() > maxNameLength) {
                maxNameLength = name.length();
            }
        }
        return maxNameLength;
    }
}
