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
    private final String workingDir;
    private final Map<String, IModule> nameModuleMap;
    private final ClassLoader servicesClassLoader;
    private URLClassLoader modulesClassLoader;

    public ModuleLoader(String workingDir, ClassLoader servicesClassLoader) {
        this.workingDir = workingDir;
        this.servicesClassLoader = servicesClassLoader;
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

        File dirModules = new File(workingDir + File.separator
                + "core" + File.separator
                + "modules");
        if (!dirModules.exists()){
            throw new ClassNotFoundException("Directory \"" + dirModules.getAbsolutePath() + "\" not found");
        }

        File fileModule = new File(dirModules.getAbsolutePath() + File.separator
                + moduleType.toLowerCase() + "-module.jar");
        if (!fileModule.exists()) {
            throw new ClassNotFoundException("File \"" + fileModule.getAbsolutePath() + "\" not found");
        }

        if (modulesClassLoader == null || nameModuleMap.isEmpty()) {
            ArrayList<URL> urls = new ArrayList<>();
            {
                File[] filesModule = dirModules.listFiles();
                for (File f : filesModule) {
                    if (!f.isFile()) continue;
                    if (!f.getName().endsWith("-module-connection.jar")) continue;
                    urls.add(f.toURI().toURL());
                }
            }
            {
                File dirLib = new File(workingDir + File.separator + "core" + File.separator + "lib");
                if (dirLib.exists()){
                    File[] filesLib = dirLib.listFiles();
                    for (File f : filesLib) {
                        if (!f.isFile()) continue;
                        if (!f.getName().endsWith(".jar")) continue;
                        urls.add(f.toURI().toURL());
                    }
                }
            }
            {
                File dirUtils = new File(workingDir + File.separator + "core" + File.separator + "utils");
                if (dirUtils.exists()){
                    File[] filesUtils = dirUtils.listFiles();
                    for (File f : filesUtils) {
                        if (!f.isFile()) continue;
                        if (!f.getName().endsWith(".jar")) continue;
                        urls.add(f.toURI().toURL());
                    }
                }
            }
            modulesClassLoader = new URLClassLoader(urls.toArray(new URL[0]), servicesClassLoader);
        }

        ClassLoader classLoader = new URLClassLoader(new URL[]{
                fileModule.toURI().toURL()
        }, modulesClassLoader);
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
