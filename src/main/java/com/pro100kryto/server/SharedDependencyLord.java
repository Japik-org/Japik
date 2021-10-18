package com.pro100kryto.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;


public class SharedDependencyLord {
    private final Path corePath;
    private final HashMap<String, SharedDependency> pathDepMap = new HashMap<>();
    private final IdentityHashMap<Tenant, ArrayList<SharedDependency>> tenantDepsMap = new IdentityHashMap<>();
    private final IdentityHashMap<SharedDependency, AtomicLong> depCounterMap = new IdentityHashMap<>();
    private final ReentrantLock locker = new ReentrantLock();

    public SharedDependencyLord(Path corePath) {
        this.corePath = corePath;
    }

    /**
     * Register a tenant witch will use a shared dependency found by a path and return it.
     * The path can be registered only once for the same tenant. Attempting register a same path for the same tenant,
     * will return a SharedDependency already registered.
     * @param tenant
     * @param filePath file path of the dependency
     * @return dependency
     * @throws java.nio.file.InvalidPathException invalid file path
     * @throws java.io.IOError failed to get absolute path
     * @throws SecurityException if not allowed checking the user.dir
     */
    public SharedDependency rentSharedDep(@NotNull Tenant tenant, @NotNull Path filePath) {
        Objects.requireNonNull(tenant);
        Objects.requireNonNull(filePath);

        final String filePathStr = filePath.toAbsolutePath().normalize().toString();

        locker.lock();

        // get or create dependency
        final SharedDependency dependency;
        if (pathDepMap.containsKey(filePathStr)){
            dependency = pathDepMap.get(filePathStr);
        } else {
            dependency = new SharedDependency(this, new File(filePathStr));
            pathDepMap.put(filePathStr, dependency);
            depCounterMap.put(dependency, new AtomicLong(0));
        }

        // check for tenant
        final ArrayList<SharedDependency> tenantDepList;
        if (tenantDepsMap.containsKey(tenant))
            tenantDepList = tenantDepsMap.get(tenant);
        else {
            tenantDepList = new ArrayList<>();
            tenantDepsMap.put(tenant, tenantDepList);
        }

        // rent
        if (!tenantDepList.contains(dependency)){
            tenantDepList.add(dependency);
            depCounterMap.get(dependency).incrementAndGet();
        }

        locker.unlock();
        return dependency;
    }

    /**
     * cancel rent
     * @param tenant
     * @param dependency
     */
    public void releaseSharedDep(Tenant tenant, SharedDependency dependency){
        locker.lock();

        @Nullable
        final AtomicLong counter = depCounterMap.get(dependency);

        // remove dep from tenant and decrement counter if removed
        {
            @Nullable
            final List<SharedDependency> tenantDepList = tenantDepsMap.get(tenant);
            if (tenantDepList != null) {
                if (tenantDepList.remove(dependency)) {
                    if (counter != null) counter.decrementAndGet();
                }
                // remove tenant
                if (tenantDepList.isEmpty()){
                    tenantDepsMap.remove(tenant);
                }
            }
        }

        // check counter value. If value is 0 then destroy dep and remove from maps
        if (counter == null || counter.get() == 0L){
            pathDepMap.remove(dependency.getFile().getPath().toString());
            depCounterMap.remove(dependency);
            releaseSharedDeps(dependency.asTenant());
            dependency.destroy();
        }

        locker.unlock();
    }

    public void releaseSharedDeps(Tenant tenant){
        locker.lock();

        while (tenantDepsMap.containsKey(tenant)){
            releaseSharedDep(tenant, tenantDepsMap.get(tenant).get(0));
        }

        locker.unlock();
    }

    public void releaseSharedDep(SharedDependency dependency){
        locker.lock();

        @Nullable
        final AtomicLong counter = depCounterMap.get(dependency);
        if (counter == null) return;

        while (counter.get() > 0){
            for (final Tenant tenant : tenantDepsMap.keySet()){
                final List<SharedDependency> tenantDepList = tenantDepsMap.get(tenant);
                if (tenantDepList.contains(dependency)){
                    releaseSharedDep(tenant, dependency);
                    break;
                }
            }
        }

        locker.unlock();
    }

    public void releaseAllSharedDeps(){
        locker.lock();

        while (!depCounterMap.isEmpty()){
            final SharedDependency dependency = depCounterMap.keySet().iterator().next();
            releaseSharedDep(dependency);
        }

        locker.unlock();
    }

    public boolean isDepLoaded(@NotNull Path path){
        return pathDepMap.containsKey(path.toAbsolutePath().toString());
    }

    public boolean isDepLoaded(@NotNull SharedDependency dependency){
        return depCounterMap.containsKey(dependency);
    }

    public Path getCorePath() {
        return corePath;
    }
}
