package com.japik.dep;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public abstract class JarDependency extends ADependency {
    protected final URL url;
    protected File file;

    protected JarDependency(DependencyLord callback, BuilderByUrl builder) {
        super(callback, builder);
        this.url = builder.getUrl();
        this.file = builder.getFile();
    }

    @Override
    protected ClassLoader createClassLoader(List<IDependency> dependencyList) throws Throwable {
        final ArrayList<ClassLoader> classLoaderList = new ArrayList<>(dependencyList.size());

        dependencyList.stream()
                .filter(iDependency -> iDependency.getDependencySide() == DependencySide.Shared)
                .map(IDependency::getClassLoader)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> classLoaderList));

        // ---

        final ArrayList<ClassLoader> implClassLoaderList = new ArrayList<>();

        dependencyList.stream()
                .filter(iDependency -> iDependency.getDependencySide() == DependencySide.Impl)
                .map(IDependency::getClassLoader)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> implClassLoaderList));

        classLoaderList.add(new JoinClassLoader(
                baseClassLoader,
                implClassLoaderList.toArray(new ClassLoader[0])
        ));

        // ----

        return new ReversedURLClassLoader(
                // src
                new URL[] {url},
                // dep
                new MultipleParentClassLoader(
                        baseClassLoader,
                        classLoaderList,
                        true
                ),
                getClassNameFilter()
        );
    }

    protected Predicate<String> getClassNameFilter() {
        return s -> true;
    }

    // -------------

    @Getter
    @Accessors(chain = true)
    public abstract static class BuilderByUrl extends ABuilder {
        @Setter
        protected URL url;
        protected File file;

        public BuilderByUrl() {
            locationType = DependencyLocationType.Jar;
        }

        @Override
        public BuilderByUrl setDependencyType(DependencyType dependencyType) {
            return (BuilderByUrl) super.setDependencyType(dependencyType);
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

        protected final void initFile() throws IOException {
            file = new File(url.getPath());
            if (!file.isFile()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        }

        @Override
        public void init() throws IOException {
            super.init();
            initFile();
        }

        @Override
        public final String getDependencyId() {
            return locationType + "|" + url;
        }
    }
}
