package com.pro100kryto.server.utils;

public final class Utils {
    public static <T> T getValueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
