package com.pro100kryto.server.dep;

import org.jetbrains.annotations.Nullable;

public final class LibJarDependency extends JarDependency implements ISharedDependency, IImplDependency{
    protected LibJarDependency(DependencyLord callback, BuilderByUrl builder) {
        super(callback, builder);
    }

    public static final class BuilderByUrl extends JarDependency.BuilderByUrl implements ISharedDependencyBuilder, IImplDependencyBuilder {

        public BuilderByUrl() {
            dependencyType = DependencyType.Lib;
        }

        @Override
        @Deprecated
        public BuilderByUrl setDependencyType(DependencyType dependencyType) {
            throw new UnsupportedOperationException();
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
