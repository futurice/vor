package com.futurice.scampiclient.utils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Array manipulation utilities
 * <p>
 * Created by apoi on 22/05/15.
 */
public class ArrayUtils {

    private static final int MAX_ARRAY_LENGTH = 100; // Drop the oldest values as this number is exceeded

    @NonNull

    public static List<Long> asList(@NonNull final long[] array) {
        final List<Long> list = new ArrayList<>();

        for (long anArray : array) {
            list.add(anArray);
        }

        return list;
    }

    @NonNull

    public static long[] asArray(@NonNull final List<Long> list) {
        final long[] array = new long[list.size()];

        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    @NonNull

    public static String asString(@NonNull final List<Long> list) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    @NonNull

    public static String asString(@NonNull final long[] array) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    @NonNull

    public static String asString(@NonNull final String[] array) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    @NonNull

    public static long[] prepend(@NonNull final long[] array, final long value) {
        // The list length will be trimmed as we receive the list over the network (simple, and more safe)
        final long[] newArray = new long[array.length + 1];
        int i = 0;

        newArray[i++] = value;
        for (long v : array) {
            newArray[i] = array[i - 1];
            i++;
        }

        return newArray;
    }

    @NonNull

    public static String[] prepend(@NonNull final String[] array, final String value) {
        // The list length will be trimmed as we receive the list over the network (simple, and more safe)
        final String[] newArray = new String[array.length + 1];
        int i = 0;

        newArray[i++] = value;
        for (String v : array) {
            newArray[i] = array[i - 1];
            i++;
        }

        return newArray;
    }

    @NonNull

    public static long[] remove(@NonNull final long[] array, final long value) {
        if (array.length < 1 || !valueExists(array, value))
            return array;
        final long[] newArray = new long[array.length - 1];
        int i = 0;
        int j = 0;

        for (long v : array) {
            if (v == value) {
                j++;
            } else {
                newArray[i] = array[j];
                i++;
                j++;
            }
        }
        return newArray;
    }


    @NonNull

    public static String[] remove(@NonNull final String[] array, final String value) {
        if (array.length < 1 || !valueExists(array, value))
            return array;
        final String[] newArray = new String[array.length - 1];
        int i = 0;
        int j = 0;

        for (String v : array) {
            if (v.equalsIgnoreCase(value)) {
                j++;
            } else {
                newArray[i] = array[j];
                i++;
                j++;
            }
        }
        return newArray;
    }

    public static boolean valueExists(@NonNull final long[] array, final long value) {
        for (long v : array) {
            if (v == value) {
                return true;
            }
        }

        return false;
    }

    public static boolean valueExists(@NonNull final String[] array, final String value) {
        for (String v : array) {
            if (v.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    @NonNull

    public static long[] parseArray(@NonNull final String s) {
        if (s.trim().length() == 0) {
            return new long[0];
        }

        final String[] tokens = s.split("\n");
        final long[] array = new long[Math.min(tokens.length, MAX_ARRAY_LENGTH)];

        for (int i = 0; i < array.length; i++) {
            array[i] = Long.valueOf(tokens[i].trim());
        }

        return array;
    }


    public static String[] parseStringArray(@NonNull final String s) {
        if (s == null)
            return new String[0];
        if (s.trim().length() == 0) {
            return new String[0];
        }

        final String[] tokens = s.split("\n");

        return tokens;
    }


    /**
     * Convert to String newline-delimited format for placing in a named field as a String
     *
     * @param array
     * @return
     */
    @NonNull

    public static String musterArray(@NonNull final long[] array) {
        final StringBuilder sb = new StringBuilder();

        for (long l : array) {
            sb.append(l);
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Convert to String newline-delimited format for placing in a named field as a String
     *
     * @param array
     * @return
     */
    @NonNull

    public static String musterArray(@NonNull final String[] array) {
        final StringBuilder sb = new StringBuilder();

        for (String s : array) {
            s.replace('\n', ' ');
            sb.append(s);
            sb.append("\n");
        }

        return sb.toString();
    }
}
