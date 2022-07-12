package com.japik.dep;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.Supplier;

public final class LibJarDependency extends JarDependency implements ISharedDependency, IImplDependency{
    @Nullable
    private final Supplier<ClassLoader> parentClassLoaderSupplier;

    protected LibJarDependency(DependencyLord callback, BuilderByUrl builder) {
        super(callback, builder);
        parentClassLoaderSupplier = builder.getParentClassLoaderSupplier();
    }

    @Override
    protected ClassLoader createClassLoader(List<IDependency> dependencyList) throws Throwable {
        return new URLClassLoader(new URL[]{url}, baseClassLoader);
    }

    @Override
    protected void loadClasses(ClassLoader classLoader) throws IOException {
        final URLClassLoader tempCL = new URLClassLoader(new URL[]{url}, null);
        tempCL.close();

        final ClassLoader loadFromCL = (
                parentClassLoaderSupplier != null && parentClassLoaderSupplier.get() != null ?
                        parentClassLoaderSupplier.get() :
                        classLoader
        );

        ClassPath.from(tempCL)
                .getAllClasses()
                .stream()
                .forEach(classInfo -> {
                    try {
                        loadFromCL.loadClass(classInfo.getName());
                    } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                    }
                });
    }

    @Getter @Setter
    @Accessors(chain = true)
    public static final class BuilderByUrl extends JarDependency.BuilderByUrl implements ISharedDependencyBuilder, IImplDependencyBuilder {
        @Nullable
        private Supplier<ClassLoader> parentClassLoaderSupplier;

        public BuilderByUrl() {
            dependencyType = DependencyType.Lib;
        }

        @Override
        @Deprecated
        public BuilderByUrl setDependencyType(DependencyType dependencyType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BuilderByUrl setUrl(URL url) {
            return (BuilderByUrl) super.setUrl(url);
        }

        @Override
        public BuilderByUrl setDependencySide(DependencySide dependencySide) {
            return (BuilderByUrl) super.setDependencySide(dependencySide);
        }

        @Override
        public BuilderByUrl setTenant(Tenant tenant) {
            return (BuilderByUrl) super.setTenant(tenant);
        }

        @Override
        public BuilderByUrl setBaseClassLoader(@Nullable ClassLoader baseClassLoader) {
            return (BuilderByUrl) super.setBaseClassLoader(baseClassLoader);
        }

        @Override
        public @Nullable ISharedDependencyBuilder getSharedDependencyBuilder() {
            return null;
        }

        @Override
        protected LibJarDependency buildDep(DependencyLord dependencyLord) throws Throwable {
            return new LibJarDependency(dependencyLord, this);
        }
    }
}
