package com.japik.dep;

import com.japik.element.ElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;

public final class ExtensionImplJarDependency extends ElementImplJarDependency {

    private ExtensionImplJarDependency(DependencyLord callback, BuilderByType builder) {
        super(callback, builder);
    }

    private ExtensionImplJarDependency(DependencyLord callback, BuilderByUrl builder) {
        super(callback, builder);
    }

    // ------------

    public final static class BuilderByType extends ElementImplJarDependency.BuilderByType {

        public BuilderByType(@NotNull Path corePath) {
            super(corePath);
            dependencyType = DependencyType.Extension;
            elementType = ElementType.Extension;
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
        public BuilderByType setElementSubtype(String elementSubtype) {
            return (BuilderByType) super.setElementSubtype(elementSubtype);
        }

        @Override
        public BuilderByType setElementVersion(@Nullable String elementVersion) {
            return (BuilderByType) super.setElementVersion(elementVersion);
        }

        @Override
        public BuilderByType setTenant(Tenant tenant) {
            return (BuilderByType) super.setTenant(tenant);
        }

        @Override
        public BuilderByType setElementName(@Nullable String elementName) {
            return (BuilderByType) super.setElementName(elementName);
        }

        @Override
        protected ElementSharedJarDependency.BuilderByType createSharedDepBuilder() {
            final ExtensionSharedJarDependency.BuilderByType builder =
                    new ExtensionSharedJarDependency.BuilderByType(corePath);
            builder
                    .setBaseClassLoader(baseClassLoader)
                    .setTenant(tenant)
                    .setElementVersion(elementVersion != null ? elementVersion.split("\\.")[0] : null)
                    .setElementSubtype(sharedSubtype);

            return builder;
        }

        @Override
        public BuilderByType setBaseClassLoader(@Nullable ClassLoader baseClassLoader) {
            return (BuilderByType) super.setBaseClassLoader(baseClassLoader);
        }

        @Override
        protected ExtensionImplJarDependency buildImplDep(DependencyLord dependencyLord) throws Throwable {
            return new ExtensionImplJarDependency(dependencyLord, this);
        }
    }

    public final static class BuilderByUrl extends ElementImplJarDependency.BuilderByUrl {

        public BuilderByUrl() {
            dependencyType = DependencyType.Extension;
            elementType = ElementType.Extension;
        }

        @Override
        @Deprecated
        public BuilderByUrl setDependencyType(DependencyType dependencyType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BuilderByUrl setCorePath(Path corePath) {
            return (BuilderByUrl) super.setCorePath(corePath);
        }

        @Override
        public BuilderByUrl setElementType(ElementType elementType) {
            return (BuilderByUrl) super.setElementType(elementType);
        }

        @Override
        public BuilderByUrl setTenant(Tenant tenant) {
            return (BuilderByUrl) super.setTenant(tenant);
        }

        @Override
        public BuilderByUrl setElementName(String elementName) {
            return (BuilderByUrl) super.setElementName(elementName);
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
        protected ExtensionSharedJarDependency.BuilderByType createSharedDepBuilder() {
            final ExtensionSharedJarDependency.BuilderByType builder =
                    new ExtensionSharedJarDependency.BuilderByType(corePath);
            builder
                    .setBaseClassLoader(baseClassLoader)
                    .setTenant(tenant)
                    .setElementVersion(elementVersion != null ? elementVersion.split("\\.")[0] : null)
                    .setElementSubtype(sharedSubtype);

            return builder;
        }

        @Override
        protected ExtensionImplJarDependency buildImplDep(DependencyLord dependencyLord) throws Throwable {
            return new ExtensionImplJarDependency(dependencyLord, this);
        }
    }
}
