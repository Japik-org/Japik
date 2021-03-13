package com.pro100kryto.server;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class URLClassLoader2 extends URLClassLoader {
    public URLClassLoader2(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public URLClassLoader2(URL[] urls) {
        super(urls);
    }

    public URLClassLoader2(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
