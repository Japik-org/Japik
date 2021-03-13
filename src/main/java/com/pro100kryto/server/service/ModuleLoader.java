package com.pro100kryto.server.service;

import com.pro100kryto.server.Constants;
import com.pro100kryto.server.UtilsInternal;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.IModule;
import org.jetbrains.annotations.Nullable;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class ModuleLoader {
    private final String workingDir;
    private final Map<String, IModule> nameModuleMap;
    private final Map<String, URLClassLoader> nameServiceLoaderMap;
    private final ILogger logger;

    public ModuleLoader(String workingDir, ILogger logger) {
        this.workingDir = workingDir;
        nameModuleMap = new HashMap<>();
        nameServiceLoaderMap = new HashMap<>();
        this.logger=logger;
    }

    /**
     * Create and contains IModule
     * @return initialized module
     */
    public synchronized IModule create(IServiceControl service, String moduleType, String moduleName)
            throws Throwable {

        if (nameModuleMap.containsKey(moduleName))
            throw new KeyAlreadyExistsException("Module with name '"+moduleName+"' already exists");

        final String className = Constants.BASE_PACKET_NAME + ".modules."+moduleType+"Module";

        final File fileModule = new File( workingDir + File.separator
                + "core" + File.separator
                + "modules"+ File.separator
                + moduleType.toLowerCase() + "-module.jar");
        if (!fileModule.exists()) {
            throw new ClassNotFoundException(fileModule.getAbsolutePath() + " not found");
        }

        // class loader
        final ArrayList<URL> urls = new ArrayList<>();
        UtilsInternal.readJarClassPathAndCheck(logger, fileModule, urls);

        final URLClassLoader classLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                getClass().getClassLoader());

        // create
        final Class<?> cls = classLoader.loadClass(className);
        if (!IModule.class.isAssignableFrom(cls))
            throw new IllegalClassFormatException("Is not assignable to IModule");

        final Constructor<?> ctor = cls.getConstructor(IServiceControl.class, String.class);
        final IModule module = (IModule) ctor.newInstance(service, moduleName);

        nameModuleMap.put(moduleName, module);
        nameServiceLoaderMap.put(moduleName, classLoader);
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
        try {
            nameServiceLoaderMap.remove(moduleName).close();
        } catch (IOException ioException){
            logger.writeException(ioException, "failed close URLClassLoader");
        }
        return nameModuleMap.remove(moduleName);
    }
}
