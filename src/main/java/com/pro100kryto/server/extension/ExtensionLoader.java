package com.pro100kryto.server.extension;

import com.pro100kryto.server.Constants;
import com.pro100kryto.server.IServerControl;

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

    public ExtensionLoader(IServerControl serverControl, String workingPath) {
        this.serverControl = serverControl;
        this.workingPath = workingPath;
    }

    public IExtension create(String type)
            throws FileNotFoundException, IllegalClassFormatException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException,
            MalformedURLException, ClassNotFoundException {

        final String className = Constants.BASE_PACKET_NAME + ".extensions."+type+"Extension";

        File fileExt = new File(workingPath + File.separator
                + "core" + File.separator
                + "extensions" + File.separator
                + type.toLowerCase() + "-extension.jar");

        if (!fileExt.exists()) {
            throw new FileNotFoundException("File \"" + fileExt.getAbsolutePath() + "\" not found");
        }

        ClassLoader classLoader = new URLClassLoader(new URL[]{
                fileExt.toURI().toURL()
        });
        Class<?> cls = classLoader.loadClass(className);
        if (!IExtension.class.isAssignableFrom(cls))
            throw new IllegalClassFormatException("Is not assignable to IExtension");

        Constructor<?> ctor = cls.getConstructor(IServerControl.class);
        IExtension extension = (IExtension) ctor.newInstance(serverControl);
        return extension;
    }
}
