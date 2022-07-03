package com.japik.utils;

import lombok.Getter;

import java.net.URL;
import java.net.URLClassLoader;

public final class EmptyClassLoader extends URLClassLoader {
    @Getter
    private static final EmptyClassLoader instance = new EmptyClassLoader();

    public EmptyClassLoader() {
        super(new URL[0]);
    }
}
