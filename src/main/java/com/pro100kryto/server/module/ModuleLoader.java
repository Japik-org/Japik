package com.pro100kryto.server.module;

import com.pro100kryto.server.Constants;
import com.pro100kryto.server.SharedDependency;
import com.pro100kryto.server.SharedDependencyLord;
import com.pro100kryto.server.Tenant;
import com.pro100kryto.server.exceptions.ManifestNotFoundException;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.service.IllegalModuleFormatException;
import com.pro100kryto.server.utils.ResolveDependenciesIncompleteException;
import com.pro100kryto.server.utils.UtilsInternal;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class ModuleLoader {
    private final IService<?> service;
    private final SharedDependencyLord sharedDependencyLord;
    private final ClassLoader baseClassLoader;
    private final ILogger logger;

    private final Map<String, IModule<?>> nameModuleMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, URLClassLoader> namePrivateDepsCLMap = new HashMap<>();
    private final Map<String, MultipleParentClassLoader> nameUnionCLMap = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();


    public ModuleLoader(IService<?> service, SharedDependencyLord sharedDependencyLord, ClassLoader baseClassLoader, ILogger logger) {
        this.service = service;
        this.sharedDependencyLord = sharedDependencyLord;
        this.baseClassLoader = baseClassLoader;
        this.logger = logger;
    }

    public <MC extends IModuleConnection> IModule<MC> createModule(String moduleType, String moduleName) throws
            ModuleAlreadyExistsException,
            IOException,
            ResolveDependenciesIncompleteException,
            IllegalModuleFormatException,
            IllegalAccessException {

        if (service.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try {

            {
                final IModule<?> module = nameModuleMap.get(moduleName);
                if (module != null) {
                    throw new ModuleAlreadyExistsException(module);
                }
            }

            // define module files
            final File moduleConnectionFile = Paths.get(sharedDependencyLord.getCorePath().toString(),
                    "modules",
                    moduleType.toLowerCase() + "-module-connection.jar").toFile();
            if (!moduleConnectionFile.exists())
                throw new FileNotFoundException(moduleConnectionFile.getCanonicalPath() + " not found");

            final File moduleFile = Paths.get(sharedDependencyLord.getCorePath().toString(),
                    "modules",
                    moduleType.toLowerCase() + "-module.jar").toFile();
            if (!moduleFile.exists())
                throw new FileNotFoundException(moduleFile.getCanonicalPath() + " not found");

            // rent module-connection
            final Tenant moduleAsTenant = new Tenant("Module name='" + moduleName + "'");
            final SharedDependency connDependency = sharedDependencyLord.rentSharedDep(
                    moduleAsTenant,
                    moduleConnectionFile.toPath()
            );

            // try resolve or release
            try {
                try {
                    ResolveDependenciesIncompleteException.Builder incompleteBuilder = new ResolveDependenciesIncompleteException.Builder();

                    // resolve
                    if (!connDependency.isResolved()) {
                        try {
                            connDependency.resolve();
                            // !! IOException !!

                        } catch (ManifestNotFoundException warningException) {
                            incompleteBuilder.addWarning(warningException);

                        } catch (ResolveDependenciesIncompleteException resolveDependenciesIncompleteException) {
                            incompleteBuilder.addCause(resolveDependenciesIncompleteException);
                            if (resolveDependenciesIncompleteException.hasErrors()) {
                                throw incompleteBuilder.build();
                            }
                        }
                    }

                    // setup private deps
                    final ArrayList<Path> privateClassPathList = new ArrayList<>();

                    try {
                        UtilsInternal.readClassPathRecursively(
                                moduleFile,
                                sharedDependencyLord.getCorePath(),
                                privateClassPathList,
                                true);

                    } catch (ManifestNotFoundException warningException) {
                        incompleteBuilder.addWarning(warningException);

                    } catch (ResolveDependenciesIncompleteException resolveDependenciesIncompleteException) {
                        incompleteBuilder.addCause(resolveDependenciesIncompleteException);
                        if (resolveDependenciesIncompleteException.hasErrors()) {
                            throw incompleteBuilder.build();
                        }
                    }

                    final URLClassLoader privateDepsClassLoader = new URLClassLoader(
                            (URL[]) Arrays.stream(privateClassPathList.toArray(new Path[0]))
                                    .map((path) -> {
                                        try {
                                            return path.toUri().toURL();
                                        } catch (MalformedURLException ignored) {
                                        }
                                        return null;
                                    }).filter(Objects::nonNull)
                                    .toArray(),
                            connDependency.getClassLoader()
                    );

                    // setup union ClassLoader
                    final MultipleParentClassLoader unionClassLoader = new MultipleParentClassLoader(
                            new ArrayList<ClassLoader>(3) {{
                                add(privateDepsClassLoader); // private deps
                                add(connDependency.getClassLoader()); // shared + connection
                                add(baseClassLoader); // parent
                            }}
                    );

                    // create module
                    final Class<?> moduleClass = Class.forName(
                            Constants.BASE_PACKET_NAME + ".modules." + moduleType + "Module",
                            true, unionClassLoader
                    );
                    if (!IModule.class.isAssignableFrom(moduleClass)) {
                        throw new IllegalClassFormatException("Is not assignable to IModule");
                    }
                    final Constructor<?> ctor = moduleClass.getConstructor(
                            ModuleParams.class
                    );
                    final IModule<MC> module = (IModule<MC>) ctor.newInstance(
                            new ModuleParams(
                                    service,
                                    moduleType, moduleName,
                                    logger,
                                    moduleAsTenant
                            )
                    );

                    // fill maps
                    nameModuleMap.put(moduleName, module);
                    namePrivateDepsCLMap.put(moduleName, privateDepsClassLoader);
                    nameUnionCLMap.put(moduleName, unionClassLoader);

                    logger.info("New module created. " + module.toString());
                    return module;

                } catch (IllegalAccessException illegalAccessException) {
                    throw illegalAccessException;

                } catch (ClassCastException |
                        ReflectiveOperationException |
                        IllegalClassFormatException formatException) {
                    throw new IllegalModuleFormatException(formatException);
                }

            } catch (Throwable throwable) {
                sharedDependencyLord.releaseSharedDeps(moduleAsTenant);
                nameModuleMap.remove(moduleName);
                nameUnionCLMap.remove(moduleName);
                try {
                    namePrivateDepsCLMap.remove(moduleName).close();
                } catch (NullPointerException ignored) {
                }
                throw throwable;
            }

        } finally {
            lock.unlock();
        }
    }

    public void deleteModule(String moduleName) throws ModuleNotFoundException {
        if (service.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try {

            @Nullable final IModule<?> module = nameModuleMap.get(moduleName);
            if (module == null) throw new ModuleNotFoundException(moduleName);

            logger.info("Deleting " + module.toString());

            if (module.getLiveCycle().getStatus().isStarted() || module.getLiveCycle().getStatus().isBroken()) {
                try {
                    module.getLiveCycle().stopForce();
                } catch (Throwable throwable) {
                    logger.exception(throwable);
                }
            }

            if (module.getLiveCycle().getStatus().isInitialized() || module.getLiveCycle().getStatus().isBroken()) {
                try {
                    module.getLiveCycle().destroy();
                } catch (Throwable throwable) {
                    logger.exception(throwable);
                }
            }

            nameModuleMap.remove(moduleName);

            try {
                nameUnionCLMap.remove(moduleName);
                namePrivateDepsCLMap.remove(moduleName).close();
            } catch (IOException ioException) {
                logger.exception(ioException, "Failed to close ClassLoader for Module name='" + moduleName + "'");
            }

            sharedDependencyLord.releaseSharedDeps(module.asTenant());

            logger.info("Module name='" + moduleName + "' deleted");

        } finally {
            lock.unlock();
        }
    }

    public IModule<?> getModule(String moduleName){
        return nameModuleMap.get(moduleName);
    }

    public Iterable<String> getModuleNames(){
        return nameModuleMap.keySet();
    }

    public Iterable<IModule<?>> getModules() {
        return nameModuleMap.values();
    }

    public int getModulesCount() {
        return nameModuleMap.size();
    }

    public boolean existsModule(String moduleName) {
        return nameModuleMap.containsKey(moduleName);
    }

    public void deleteAllModules(){
        if (service.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try{

            while (!nameModuleMap.isEmpty()){
                try {
                    deleteModule(nameModuleMap.keySet().iterator().next());
                } catch (ModuleNotFoundException ignored) {
                }
            }

        } finally {
            lock.unlock();
        }
    }

    public static final class Builder{
        private final SharedDependencyLord sharedDependencyLord;
        private final ClassLoader baseClassLoader;
        private final ILogger logger;

        public Builder(SharedDependencyLord sharedDependencyLord, ClassLoader baseClassLoader, ILogger logger) {
            this.sharedDependencyLord = sharedDependencyLord;
            this.baseClassLoader = baseClassLoader;
            this.logger = logger;
        }

        public ModuleLoader build(IService<?> service){
            return new ModuleLoader(service,
                    sharedDependencyLord, baseClassLoader, logger);
        }
    }
}