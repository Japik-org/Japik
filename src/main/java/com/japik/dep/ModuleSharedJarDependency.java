package com.japik.dep;

import com.japik.element.ElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public final class ModuleSharedJarDependency extends ElementSharedJarDependency {

    private ModuleSharedJarDependency(DependencyLord callback, BuilderByType builder) {
        super(callback, builder);
    }

    private ModuleSharedJarDependency(DependencyLord callback, BuilderByUrl builder) {
        super(callback, builder);
    }

    // ------------

    public final static class BuilderByType extends ElementSharedJarDependency.BuilderByType {

        public BuilderByType(@NotNull Path corePath) {
            super(corePath);
            dependencyType = DependencyType.Module;
            elementType = ElementType.Module;
        }

        @Override
        @Deprecated
        public BuilderByType setDependencyType(DependencyType dependencyType) {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        public BuilderByType setElementType(ElementType elementType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BuilderByType setBaseClassLoader(@Nullable ClassLoader baseClassLoader) {
            return (BuilderByType) super.setBaseClassLoader(baseClassLoader);
        }

        @Override
        public BuilderByType setElementName(@Nullable String elementName) {
            return (BuilderByType) super.setElementName(elementName);
        }

        @Override
        public BuilderByType setTenant(Tenant tenant) {
            return (BuilderByType) super.setTenant(tenant);
        }

        @Override
        public BuilderByType setElementSubtype(String elementSubtype) {
            return (BuilderByType) super.setElementSubtype(elementSubtype);
        }

        @Override
        public BuilderByType setElementVersion(@Nullable String elementVersion) {
            return (BuilderByType) super.setElementVersion(elementVersion);
        }

        @Override
        protected ModuleSharedJarDependency buildDep(DependencyLord dependencyLord) throws IOException {
            return new ModuleSharedJarDependency(dependencyLord, this);
        }
    }

    public final static class BuilderByUrl extends ElementSharedJarDependency.BuilderByUrl {

        public BuilderByUrl() {
            dependencyType = DependencyType.Module;
            elementType = ElementType.Module;
        }

        @Override
        @Deprecated
        public BuilderByUrl setDependencyType(DependencyType dependencyType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BuilderByUrl setTenant(Tenant tenant) {
            return (BuilderByUrl) super.setTenant(tenant);
        }

        @Override
        public BuilderByUrl setElementType(ElementType elementType) {
            return (BuilderByUrl) super.setElementType(elementType);
        }

        @Override
        public BuilderByUrl setUrl(URL url) {
            return (BuilderByUrl) super.setUrl(url);
        }

        @Override
        public BuilderByUrl setBaseClassLoader(@Nullable ClassLoader baseClassLoader) {
            return (BuilderByUrl) super.setBaseClassLoader(baseClassLoader);
        }

        @Override
        public BuilderByUrl setElementName(@Nullable String elementName) {
            return (BuilderByUrl) super.setElementName(elementName);
        }

        @Override
        protected ModuleSharedJarDependency buildDep(DependencyLord dependencyLord) throws Throwable {
            return new ModuleSharedJarDependency(dependencyLord, this);
        }
    }
}
