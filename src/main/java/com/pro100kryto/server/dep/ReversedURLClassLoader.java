package com.pro100kryto.server.dep;


import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.function.Predicate;

public class ReversedURLClassLoader extends URLClassLoader {
    private final Predicate<String> nameFilter;

    public ReversedURLClassLoader(URL[] urls, ClassLoader parent, Predicate<String> nameFilter) {
        super(urls, parent);
        this.nameFilter = nameFilter;
    }

    public ReversedURLClassLoader(URL[] urls, Predicate<String> nameFilter) {
        super(urls);
        this.nameFilter = nameFilter;
    }

    public ReversedURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory, Predicate<String> nameFilter) {
        super(urls, parent, factory);
        this.nameFilter = nameFilter;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);

            if (c == null) {
                try {
                    long t0 = System.nanoTime();
                    c = findClass(name);
                    // this is the defining class loader; record the stats
                    //sun.misc.PerfCounter.getParentDelegationTime().addTime(0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t0);
                    sun.misc.PerfCounter.getFindClasses().increment();
                } catch (ClassNotFoundException ignored) {
                }

                if (c == null) {
                    if (getParent() != null) {
                        c = getParent().loadClass(name);
                    } else {
                        throw new ClassNotFoundException(name);
                    }
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!nameFilter.test(name)) {
            throw new ClassNotFoundException(name);
        }

        return super.findClass(name);
    }
}
