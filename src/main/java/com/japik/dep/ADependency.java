package com.japik.dep;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ADependency implements IDependency{
    protected final ReentrantLock lock = new ReentrantLock();
    protected final DependencyLord callback;

    protected final String id;
    protected final DependencyLocationType locationType;

    protected final DependencyType dependencyType;
    protected final DependencySide dependencySide;

    @Nullable
    protected final ClassLoader baseClassLoader;

    private final ArrayList<Tenant> tenantList = new ArrayList<>(1);

    private DependencyStatus status = DependencyStatus.NOT_RESOLVED;

    @Nullable
    protected List<IDependency> dependencyList;
    @Nullable
    protected ClassLoader classLoader;

    protected ADependency(DependencyLord callback,
                       ABuilder builder) {
        this.callback = callback;

        this.id = builder.getDependencyId();
        this.locationType = builder.getLocationType();
        this.dependencyType = builder.getDependencyType();
        this.dependencySide = builder.getDependencySide();
        this.baseClassLoader = builder.getBaseClassLoader();

        tenantList.add(builder.getTenant());
    }

    @Override
    public final void resolveAll() throws ResolveDependencyException {
        if (status != DependencyStatus.NOT_RESOLVED) return;

        lock.lock();
        try {
            if (status != DependencyStatus.NOT_RESOLVED) return;

            resolveClassLoaders();

            resolveClasses();

        } catch (ResolveDependencyException resolveDependencyException) {
            throw resolveDependencyException;

        } catch (Throwable throwable) {
            throw new ResolveDependencyException(throwable);

        } finally {
            if (status == DependencyStatus.RESOLVING) {
                status = DependencyStatus.NOT_RESOLVED;
            }
            lock.unlock();
        }
    }

    @Override
    public final void resolveClassLoaders() throws Throwable {
        if (status != DependencyStatus.NOT_RESOLVED) return;

        lock.lock();
        try {
            if (status != DependencyStatus.NOT_RESOLVED) return;
            status = DependencyStatus.RESOLVING;

            final ArrayList<IDependency> dependencyList = new ArrayList<>(0);
            addDependencies(dependencyList);
            this.dependencyList = Collections.unmodifiableList(dependencyList);

            for (final IDependency dep : dependencyList) {
                dep.resolveClassLoaders();
            }

            if (classLoader == null) {
                classLoader = createClassLoader(dependencyList);
                if (classLoader == null) {
                    throw new NullPointerException("classLoader is null");
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final void resolveClasses() throws ReflectiveOperationException, LinkageError, IOException {
        lock.lock();
        try {
            for (final IDependency dep : dependencyList) {
                dep.resolveClasses();
            }
            loadClasses(classLoader);

            status = DependencyStatus.RESOLVED;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final void destroy() {
        if (status == DependencyStatus.DESTROYED) return;

        lock.lock();
        try {
            if (status == DependencyStatus.DESTROYED) return;
            status = DependencyStatus.DESTROYED;

            callback.release(this);

            releaseResources();

        } finally {
            lock.unlock();
        }
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final DependencyLocationType getLocationType() {
        return locationType;
    }

    @Override
    public final DependencyType getDependencyType() {
        return dependencyType;
    }

    @Override
    public final DependencySide getDependencySide() {
        return dependencySide;
    }

    @Override
    public final List<Tenant> getTenantList() {
        return tenantList;
    }

    @Override
    public final DependencyStatus getStatus() {
        return status;
    }

    @Override
    public final List<IDependency> getDependencyList() {
        return dependencyList;
    }

    @Override
    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    protected void addDependencies(ArrayList<IDependency> dependencyListOut) throws Throwable {
    }

    protected abstract ClassLoader createClassLoader(List<IDependency> dependencyList) throws Throwable;

    protected abstract void loadClasses(ClassLoader classLoader) throws IOException;

    protected void releaseResources() {
        if (dependencyList != null) {
            dependencyList = null;
        }
        tenantList.clear();
        classLoader = null;
    }

    // --------------

    @Getter
    @Accessors(chain = true)
    public abstract static class ABuilder implements IDependencyBuilder {
        protected DependencyLocationType locationType;

        @Setter
        protected DependencyType dependencyType;
        @Setter
        protected DependencySide dependencySide;
        @Setter
        protected Tenant tenant;

        @Setter @Nullable
        protected ClassLoader baseClassLoader;

        protected boolean isInit = false;

        protected boolean isNotValid() {
            return locationType == null || dependencyType == null || dependencySide == null || tenant == null;
        }

        public void init() throws IOException {
            isInit = true;
        }

        @Override
        public final JarDependency build(DependencyLord dependencyLord) throws Throwable {
            if (isNotValid()) {
                throw new IllegalStateException("Invalid fields. "+this.toString());
            }

            init();

            return buildDep(dependencyLord);
        }

        protected abstract JarDependency buildDep(DependencyLord dependencyLord) throws Throwable;

        @Override
        public String toString() {
            return "DependencyBuilder { dependencyId:'"+ getDependencyId() +"' dependencyType:'"+dependencyType+"' dependencySide:'"+dependencySide+"' tenant:'"+tenant+"' }";
        }
    }

}
