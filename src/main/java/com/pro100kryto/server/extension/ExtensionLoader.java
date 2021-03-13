package com.pro100kryto.server.extension;

import com.pro100kryto.server.Constants;
import com.pro100kryto.server.IServerControl;
import com.pro100kryto.server.URLClassLoader2;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public final class ExtensionLoader {
    private final IServerControl serverControl;
    private final String workingPath;
    private final URLClassLoader2 parentClassLoader;

    public ExtensionLoader(IServerControl serverControl, URLClassLoader2 parentClassLoader, String workingPath) {
        this.serverControl = serverControl;
        this.workingPath = workingPath;
        this.parentClassLoader = parentClassLoader;
    }

    public IExtension create(String type)
            throws FileNotFoundException, IllegalClassFormatException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException,
            MalformedURLException, ClassNotFoundException {

        final String className = Constants.BASE_PACKET_NAME + ".extensions."+type+"Extension";

        final File fileExt = new File(workingPath + File.separator
                + "core" + File.separator
                + "extensions" + File.separator
                + type.toLowerCase() + "-extension.jar");

        if (!fileExt.exists()) {
            throw new FileNotFoundException("File \"" + fileExt.getAbsolutePath() + "\" not found");
        }

        final ClassLoader classLoader = new URLClassLoader(new URL[]{
                fileExt.toURI().toURL(),
        }, parentClassLoader);
        Class<?> cls = classLoader.loadClass(className);
        if (!IExtension.class.isAssignableFrom(cls))
            throw new IllegalClassFormatException("Is not assignable to IExtension");

        final Constructor<?> ctor = cls.getConstructor(IServerControl.class);
        final IExtension extension = (IExtension) ctor.newInstance(serverControl);
        return extension;
    }
}
