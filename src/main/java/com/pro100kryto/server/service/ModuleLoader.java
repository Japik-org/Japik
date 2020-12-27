package com.pro100kryto.server.service;

import com.pro100kryto.server.Constants;
import com.pro100kryto.server.module.IModule;
import com.sun.istack.Nullable;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class ModuleLoader {
    private final String workingPath;
    private final Map<String, IModule> nameModuleMap;

    public ModuleLoader(String workingPath) {
        this.workingPath = workingPath;
        nameModuleMap = new HashMap<>();
    }

    /**
     * Create and contains IModule
     * @return initialized module
     */
    public synchronized IModule create(IServiceControl service, String moduleType, String moduleName)
            throws ClassNotFoundException, IllegalClassFormatException, InvocationTargetException,
            KeyAlreadyExistsException, MalformedURLException, NoSuchMethodException,
            IllegalAccessException, InstantiationException {

        if (nameModuleMap.containsKey(moduleName))
            throw new KeyAlreadyExistsException("Module with name '"+moduleName+"' already exists");

        final String className = Constants.BASE_PACKET_NAME + ".modules."+moduleType+"Module";
        final ArrayList<URL> urls = new ArrayList<>();

        File dirModules = new File(workingPath + File.separator
                + "core" + File.separator
                + "modules");
        if (!dirModules.exists()){
            throw new ClassNotFoundException("Directory \"" + dirModules.getAbsolutePath() + "\" not found");
        }

        File fileModule = new File(dirModules.toURI().toURL() + File.separator
                + moduleType.toLowerCase() + "-module.jar");
        if (!fileModule.exists()) {
            throw new ClassNotFoundException("File \"" + fileModule.getAbsolutePath() + "\" not found");
        }
        urls.add(fileModule.toURI().toURL());

        // temp solution!!
        File[] moduleFiles = dirModules.listFiles();
        for (File f : moduleFiles){
            if (!f.isFile()) continue;
            if (!f.getName().endsWith("-module-connection.jar")) continue;
            urls.add(f.toURI().toURL());
        }

        ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]));
        Class<?> cls = classLoader.loadClass(className);
        if (!IModule.class.isAssignableFrom(cls))
            throw new IllegalClassFormatException("Is not assignable to IModule");

        Constructor<?> ctor = cls.getConstructor(IServiceControl.class, String.class);
        IModule module = (IModule) ctor.newInstance(service, moduleName);

        nameModuleMap.put(moduleName, module);
        return module;
    }

    public synchronized boolean exists(String moduleName){
        return nameModuleMap.containsKey(moduleName);
    }

    @Nullable
    public synchronized IModule getModule(String moduleName){
        return nameModuleMap.get(moduleName);
    }

    @Nullable
    public synchronized IModule removeModule(String moduleName){
        return nameModuleMap.remove(moduleName);
    }
}
