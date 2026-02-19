package com.github.baibeicha.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        int strLen;

        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String multiply(String str, int amount) {
        return multiply(str, (long) amount);
    }

    public static String multiply(String str, long amount) {
        if (isEmpty(str)) {
            throw new IllegalArgumentException();
        }
        if (amount < 0) {
            throw new IllegalArgumentException();
        } else if (amount == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            sb.append(str);
        }

        return sb.toString();
    }

    public static String stringOf(Character... str) {
        StringBuilder sb = new StringBuilder();
        for (Character c : str) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static String format(String template, String delimiter, Object... args) {
        if ("{}".equals(delimiter)) {
            int length = args.length;
            boolean[] used = new boolean[length];
            int autoIndex = 0;
            Pattern pattern = Pattern.compile("\\{(\\d+)?}");
            Matcher matcher = pattern.matcher(template);
            StringBuilder sb = new StringBuilder();
            int start = 0;

            while (matcher.find()) {
                sb.append(template, start, matcher.start());
                String group = matcher.group(1);
                if (group != null) {
                    try {
                        int index = Integer.parseInt(group);
                        if (index >= 0 && index < length) {
                            sb.append(args[index]);
                            used[index] = true;
                        } else {
                            sb.append(matcher.group());
                        }
                    } catch (NumberFormatException e) {
                        sb.append(matcher.group());
                    }
                } else {
                    while (autoIndex < length && used[autoIndex]) {
                        autoIndex++;
                    }
                    if (autoIndex < length) {
                        sb.append(args[autoIndex]);
                        used[autoIndex] = true;
                        autoIndex++;
                    } else {
                        sb.append("{}");
                    }
                }
                start = matcher.end();
            }
            sb.append(template.substring(start));

            for (int i = 0; i < length; i++) {
                if (!used[i]) {
                    sb.append(args[i]);
                }
            }

            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            int start = 0;
            int argIndex = 0;
            int delimLength = delimiter.length();
            while (start < template.length()) {
                int end = template.indexOf(delimiter, start);
                if (end == -1) {
                    break;
                }
                sb.append(template, start, end);
                if (argIndex < args.length) {
                    sb.append(args[argIndex++]);
                } else {
                    sb.append(delimiter);
                }
                start = end + delimLength;
            }
            sb.append(template.substring(start));
            if (argIndex < args.length) {
                for (int i = argIndex; i < args.length; i++) {
                    sb.append(args[i]);
                }
            }
            return sb.toString();
        }
    }

    public static String format(String template, Object... args) {
        return format(template, "{}", args);
    }
}
