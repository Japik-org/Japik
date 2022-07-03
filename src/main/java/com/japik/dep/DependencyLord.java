package com.japik.dep;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public final class DependencyLord {
    @Getter
    private final Path corePath;

    private final HashMap<String, IDependency> idDepMap = new HashMap<>();
    private final HashMap<Tenant, ArrayList<IDependency>> tenantDepListMap = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public boolean isLoadedAsShared(String depId) {
        lock.lock();
        try {
            return idDepMap.containsKey(depId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * for impl dependencies
     */
    public IDependency buildImpl(IImplDependencyBuilder implDepBuilder) throws Throwable {
        final IImplDependency implDependency = (IImplDependency) implDepBuilder.build(this);
        return implDependency;
    }

    /**
     * for shared dependencies
     */
    public IDependency rentShared(ISharedDependencyBuilder sharedDepBuilder) throws Throwable {

        if (!sharedDepBuilder.isInit()) {
            sharedDepBuilder.init();
        }

        final String depId = Objects.requireNonNull(sharedDepBuilder.getDependencyId());
        final Tenant tenant = Objects.requireNonNull(sharedDepBuilder.getTenant());

        lock.lock();
        try {
            final IDependency dep;

            if (idDepMap.containsKey(depId)) {
                dep = idDepMap.get(depId);
                dep.getTenantList().add(tenant);
            } else {
                dep = sharedDepBuilder.build(this);
                idDepMap.put(depId, dep);
            }

            if (tenantDepListMap.containsKey(tenant)) {
                tenantDepListMap.get(tenant).add(dep);
            } else {
                tenantDepListMap.put(tenant, new ArrayList<IDependency>(1){{
                    add(dep);
                }});
            }

            return dep;

        } finally {
            lock.unlock();
        }
    }

    public void release(IDependency dependency) {
        lock.lock();
        try{

            idDepMap.remove(dependency.getId());

            for (final Tenant tn: dependency.getTenantList())
            {
                @Nullable
                final ArrayList<IDependency> dependencyList = tenantDepListMap.get(tn);
                if (dependencyList != null) {
                    dependencyList.remove(dependency);
                }
            }

            if (dependency.getStatus() != DependencyStatus.DESTROYED) {
                dependency.destroy();
            }

        } finally {
            lock.unlock();
        }
    }

    public void release(Tenant tenant) {
        lock.lock();

        @Nullable
        final ArrayList<IDependency> dependencyList = tenantDepListMap.get(tenant);
        if (dependencyList != null) {

            while (true) {
                boolean isDone = true;
                for (final IDependency dep : dependencyList) {
                    dep.getTenantList().remove(tenant);
                    if (dep.getTenantList().isEmpty()) {
                        dep.destroy();
                        isDone = false;
                        dependencyList.remove(dep);
                        break;
                    }
                }
                if (isDone) break;
            }
            dependencyList.clear();

        }

        lock.unlock();
    }

    public void releaseAll() {
        while (!idDepMap.isEmpty()){
            release(idDepMap.values().iterator().next());
        }
    }
}
