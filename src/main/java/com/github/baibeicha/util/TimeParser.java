package com.github.baibeicha.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

    public static String DEFAULT_SEPARATOR = " ";

    public static long parseTime(String timeStr) {
        return parseTime(timeStr, DEFAULT_SEPARATOR);
    }

    public static long parseTime(String timeStr, String separator) {
        String[] times = timeStr.split(separator);

        long result = 0;
        for (String time : times) {
            if (!time.isEmpty()) {
                result += parsePart(time.trim());
            }
        }

        return result;
    }

    private static long parsePart(String timeStr) {
        Pattern pattern = Pattern.compile("^(\\d+)(ms|mn|[smhdwy])?$");
        Matcher matcher = pattern.matcher(timeStr);

        if (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            if (unit == null) {
                unit = "ms";
            }

            return convertToMillis(value, unit);
        } else {
            throw new IllegalArgumentException("Invalid time format: " + timeStr);
        }
    }

    private static long convertToMillis(long value, String unit) {
        return switch (unit) {
            case "ms" -> value;
            case "s"  -> value * 1000;
            case "m"  -> value * 60 * 1000;
            case "h"  -> value * 60 * 60 * 1000;
            case "d"  -> value * 24 * 60 * 60 * 1000;
            case "w"  -> value * 7 * 24 * 60 * 60 * 1000;
            case "mn" -> value * 30 * 24 * 60 * 60 * 1000;
            case "y"  -> value * 365 * 24 * 60 * 60 * 1000;
            default -> value;
        };
    }
}
