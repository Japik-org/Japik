package com.pro100kryto.server.extension;

import com.pro100kryto.server.*;
import com.pro100kryto.server.exceptions.ManifestNotFoundException;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.utils.ResolveDependenciesIncompleteException;
import com.pro100kryto.server.utils.UtilsInternal;
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

public final class ExtensionLoader {
    private final Server server;
    private final SharedDependencyLord sharedDependencyLord;
    private final ClassLoader baseClassLoader;
    private final ILogger logger;

    private final Map<String, IExtension<?>> typeExtMap = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, URLClassLoader> typePrivateCLMap = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    public ExtensionLoader(Server server, SharedDependencyLord sharedDependencyLord, ClassLoader baseClassLoader, ILogger logger) {
        this.server = server;
        this.sharedDependencyLord = sharedDependencyLord;
        this.baseClassLoader = baseClassLoader;
        this.logger = logger;
    }

    public <EC extends IExtensionConnection> IExtension<EC> createExtension(String extType) throws
            ExtensionAlreadyExistsException,
            IOException,
            ResolveDependenciesIncompleteException,
            IllegalExtensionFormatException,
            IllegalAccessException{

        if (server.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try {

            {
                final IExtension<?> existingExt = typeExtMap.get(extType);
                if (existingExt != null) {
                    throw new ExtensionAlreadyExistsException(existingExt);
                }
            }

            // define ext files
            final File extConnectionFile = Paths.get(sharedDependencyLord.getCorePath().toString(),
                    "extensions",
                    extType.toLowerCase() + "-extension-connection.jar").toFile();
            if (!extConnectionFile.exists()) {
                logger.warn(extConnectionFile.getCanonicalPath() + " not found");
            }

            final File extFile = Paths.get(sharedDependencyLord.getCorePath().toString(),
                    "extensions",
                    extType.toLowerCase() + "-extension.jar").toFile();
            if (!extFile.exists()) {
                throw new FileNotFoundException(extFile.getCanonicalPath() + " not found");
            }

            // rent ext conn
            final Tenant extAsTenant = new Tenant("Extension type='" + extType + "'");
            final SharedDependency connDependency = sharedDependencyLord.rentSharedDep(
                    extAsTenant,
                    extConnectionFile.toPath()
            );

            // try resolve or release
            try {
                try {
                    ResolveDependenciesIncompleteException.Builder incompleteBuilder = new ResolveDependenciesIncompleteException.Builder();

                    // resolve
                    if (extConnectionFile.exists() && !connDependency.isResolved()) {
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
                                extFile,
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
                                    .toArray(URL[]::new),
                            (connDependency.isResolved() ? connDependency.getClassLoader() : baseClassLoader)
                    );

                    // load ext class
                    final String extPkgName = Constants.BASE_PACKET_NAME + ".extensions." + extType.toLowerCase();
                    final Class<?> extClass = Class.forName(
                            extPkgName + "." + extType + "Extension",
                            true, privateDepsClassLoader
                    );
                    if (!IExtension.class.isAssignableFrom(extClass)) {
                        throw new IllegalClassFormatException("Is not assignable to IExtension");
                    }
                    final Constructor<?> ctor = extClass.getConstructor(
                            ExtensionParams.class
                    );

                    // load packages
                    if (connDependency.isResolved()){
                        UtilsInternal.loadAllClasses(
                                connDependency.getClassLoader(),
                                extConnectionFile.toURI().toURL(),
                                extPkgName+".connection"
                        );
                    }
                    UtilsInternal.loadAllClasses(
                            privateDepsClassLoader,
                            extFile.toURI().toURL(),
                            extPkgName
                    );

                    // create ext object
                    final IExtension<EC> extension = (IExtension<EC>) ctor.newInstance(
                            new ExtensionParams(
                                    server,
                                    extType,
                                    logger,
                                    extAsTenant
                            )
                    );

                    // fill maps
                    typeExtMap.put(extType, extension);
                    typePrivateCLMap.put(extType, privateDepsClassLoader);

                    logger.info("New extension created. " + extension.toString());
                    return extension;

                } catch (IllegalAccessException illegalAccessException) {
                    throw illegalAccessException;

                } catch (ClassCastException |
                        ReflectiveOperationException |
                        IllegalClassFormatException formatException) {
                    throw new IllegalExtensionFormatException(formatException);
                }

            } finally {
                sharedDependencyLord.releaseSharedDeps(extAsTenant);
                typeExtMap.remove(extType);
                try {
                    typePrivateCLMap.remove(extType).close();
                } catch (NullPointerException ignored) {
                }
            }

        } finally {
            lock.unlock();
        }
    }

    public void deleteExtension(String extType) throws ExtensionNotFoundException {
        if (server.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try {

            @Nullable final IExtension<?> extension = typeExtMap.get(extType);
            if (extension == null) {
                throw new ExtensionNotFoundException(extType);
            }

            logger.info("Deleting " + extension.toString());

            if (extension.getLiveCycle().getStatus().isStarted() || extension.getLiveCycle().getStatus().isBroken()) {
                try {
                    extension.getLiveCycle().stopForce();
                } catch (Throwable throwable) {
                    logger.exception(throwable);
                }
            }

            typeExtMap.remove(extType);

            try {
                typePrivateCLMap.remove(extType).close();
            } catch (IOException ioException) {
                logger.exception(ioException, "Failed to close ClassLoader for Extension type='" + extType + "'");
            }

            sharedDependencyLord.releaseSharedDeps(extension.asTenant());

            logger.info("Extension type='" + extType + "' deleted");

        } finally {
            lock.unlock();
        }
    }

    public boolean existsExtension(String extType){
        return typeExtMap.containsKey(extType);
    }

    public IExtension<?> getExtension(String extType){
        return typeExtMap.get(extType);
    }

    public Iterable<String> getExtensionTypes(){
        return typeExtMap.keySet();
    }

    public Iterable<IExtension<?>> getExtensions(){
        return typeExtMap.values();
    }

    public int getExtensionsCount(){
        return typeExtMap.size();
    }

    public void deleteAllExtensions(){
        if (server.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }

        lock.lock();
        try{

            while (!typeExtMap.isEmpty()){
                try {
                    deleteExtension(typeExtMap.keySet().iterator().next());
                } catch (ExtensionNotFoundException ignored) {
                }
            }

        } finally {
            lock.unlock();
        }
    }
}
