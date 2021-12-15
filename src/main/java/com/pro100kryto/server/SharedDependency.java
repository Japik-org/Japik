package com.pro100kryto.server;

import com.pro100kryto.server.exceptions.ManifestNotFoundException;
import com.pro100kryto.server.utils.EmptyClassLoader;
import com.pro100kryto.server.utils.ResolveDependenciesIncompleteException;
import com.pro100kryto.server.utils.TransformedUnmodifiableList;
import com.pro100kryto.server.utils.UtilsInternal;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class SharedDependency {
    private final ReentrantLock statusLocker = new ReentrantLock();
    private final SharedDependencyLord sharedDependencyLord;
    private final Tenant tenant;
    private final File file;
    private final String basePackage;
    private URLClassLoader fileClassLoader;
    private List<SharedDependency> dependencyList;
    private MultipleParentClassLoader unionClassLoader;
    private Status status = Status.NOT_RESOLVED;

    /**
     * @param sharedDependencyLord
     * @param file dependency file for load classes
     * @throws InvalidPathException invalid path for file
     * @throws NullPointerException
     */
    public SharedDependency(SharedDependencyLord sharedDependencyLord, File file) {
        this(sharedDependencyLord, file, null);
    }

    /**
     * @param sharedDependencyLord
     * @param file dependency file for load classes
     * @throws InvalidPathException invalid path for file
     * @throws NullPointerException
     */
    public SharedDependency(SharedDependencyLord sharedDependencyLord, File file, String basePackage) {
        this.sharedDependencyLord = sharedDependencyLord;
        tenant = new Tenant(file.getAbsolutePath());
        this.file = file;
        this.basePackage = basePackage;
    }

    /**
     * Resolve dependencies and create class loader
     * @throws IOException - FileNotFoundException, etc.
     * @throws ManifestNotFoundException
     * @throws ResolveDependenciesIncompleteException - current dependency was resolved with warnings or incompletely
     * @throws IllegalStateException - already resolving or destroyed
     */
    public void resolve() throws IOException, ManifestNotFoundException, ResolveDependenciesIncompleteException {
        statusLocker.lock();

        try {
            // check
            if (status == Status.RESOLVING) throw new IllegalStateException("Already resolving");
            if (status == Status.DESTROYED) throw new IllegalStateException("Is destroyed");
            status = Status.RESOLVING;

            try {
                final ResolveDependenciesIncompleteException.Builder incompleteBuilder = new ResolveDependenciesIncompleteException.Builder();
                if (!file.exists()) throw new FileNotFoundException("File " + file.toPath() + " not found");

                // resolving

                // dependencies
                if (dependencyList == null) {

                    try {
                        final ArrayList<Path> depPaths = new ArrayList<>();

                        // !! IOException !!
                        UtilsInternal.readClassPathRecursively(file, sharedDependencyLord.getCorePath(), depPaths, false);

                        dependencyList = new ArrayList<>(depPaths.size());
                        for (final Path depPath : depPaths) {
                            dependencyList.add(sharedDependencyLord.rentSharedDep(tenant, depPath));
                        }

                    } catch (ResolveDependenciesIncompleteException incompleteException){
                        incompleteBuilder.addCause(incompleteException);

                    } catch (ManifestNotFoundException ignored){
                    }

                    if (dependencyList == null){
                        dependencyList = new ArrayList<>(0);
                    }
                }

                // Union ClassLoader
                if (unionClassLoader == null) {
                    unionClassLoader = new MultipleParentClassLoader(
                            ClassLoader.getSystemClassLoader(),
                            new TransformedUnmodifiableList<>(
                                    dependencyList,
                                    SharedDependency::getClassLoader,
                                    EmptyClassLoader.getInstance()
                            ),
                            true
                    );
                }

                // file ClassLoader
                if (fileClassLoader == null) {
                    fileClassLoader = new URLClassLoader(
                            new URL[]{file.toURI().toURL()},
                            unionClassLoader
                    );
                }

                // Resolve recursively
                for (final SharedDependency dependency : dependencyList) {
                    if (dependency.getStatus() == Status.RESOLVING) continue; // avoid cycling/looping

                    try {
                        dependency.resolve();

                    } catch (ResolveDependenciesIncompleteException incompleteException1){
                        incompleteBuilder.addCause(incompleteException1);

                    } catch (Throwable throwable) {
                        // Failed to resolve additional shared dependency
                        incompleteBuilder.addError(throwable);
                    }
                }

                // force load classes
                if (basePackage!=null){
                    UtilsInternal.loadAllClasses(
                            fileClassLoader,
                            file.toURI().toURL(),
                            basePackage
                    );
                } else {
                    UtilsInternal.loadAllClasses(
                            fileClassLoader,
                            file.toURI().toURL()
                    );
                }

                // throw collected warnings
                if (!incompleteBuilder.isEmpty()){
                    throw incompleteBuilder.build();
                }

                // without errors
                status = Status.RESOLVED;

            } catch (ResolveDependenciesIncompleteException incompleteException) {
                if (!incompleteException.hasErrors()) {
                    status = Status.RESOLVED;
                }
                throw incompleteException;
            }

        } finally {
            if (status == Status.RESOLVING){
                status = Status.NOT_RESOLVED;
            }
            statusLocker.unlock();
        }
    }

    public void release(Tenant tenant){
        statusLocker.lock();
        sharedDependencyLord.releaseSharedDep(tenant, this);
        statusLocker.unlock();
    }

    public void destroy() {
        if (status == Status.DESTROYED) return;

        statusLocker.lock();
        try {
            status = Status.DESTROYED;
            if (sharedDependencyLord.isDepLoaded(this))
                sharedDependencyLord.releaseSharedDep(this);

            try {
                fileClassLoader.close();
            } catch (IOException | NullPointerException ignored) {
            }
            fileClassLoader = null;
            unionClassLoader = null;
            dependencyList = null;

        } finally {
            statusLocker.unlock();
        }
    }

    public File getFile() {
        return file;
    }

    @Nullable
    public ClassLoader getClassLoader() {
        return fileClassLoader;
    }

    public List<SharedDependency> getDependencyList() {
        return dependencyList;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isResolved(){
        return status == Status.RESOLVED;
    }

    public Tenant asTenant() {
        return tenant;
    }

    public enum Status{
        NOT_RESOLVED,
        RESOLVING,
        RESOLVED,
        DESTROYED
    }
}
