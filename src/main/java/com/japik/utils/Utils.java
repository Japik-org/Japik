package com.japik.utils;

public final class Utils {
    public static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
