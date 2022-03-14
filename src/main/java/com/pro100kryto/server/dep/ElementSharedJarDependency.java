package com.pro100kryto.server.dep;

import com.pro100kryto.server.NotImplementedException;
import com.pro100kryto.server.element.ElementType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.util.function.Predicate;

@Getter
public abstract class ElementSharedJarDependency extends ElementJarDependency implements ISharedDependency {

    protected ElementSharedJarDependency(DependencyLord callback, BuilderByType builder) {
        super(callback, builder);
    }

    protected ElementSharedJarDependency(DependencyLord callback, BuilderByUrl builder) {
        super(callback, builder);
    }

    @Override
    protected @Nullable Predicate<String> getClassNameFilter() {
        final String pkg = basePackage+"."+
                elementType.toString().toLowerCase()+"s.";

        return s -> s.startsWith(pkg+elementSubtype.toLowerCase()+".connection") ||
                s.startsWith(pkg+elementSubtype.toLowerCase()+".shared");
    }

    public static class BuilderByType extends ElementJarDependency.BuilderByType implements ISharedDependencyBuilder {

        public BuilderByType(@NotNull Path corePath) {
            super(corePath);
            dependencySide = DependencySide.Shared;
        }

        @Override
        @Deprecated
        public BuilderByType setDependencySide(DependencySide dependencySide) {
            throw new UnsupportedOperationException();
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
        public BuilderByType setElementType(ElementType elementType) {
            return (BuilderByType) super.setElementType(elementType);
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
        protected ElementSharedJarDependency buildDep(DependencyLord dependencyLord) throws Throwable {
            throw new NotImplementedException();
        }
    }

    public abstract static class BuilderByUrl extends ElementJarDependency.BuilderByUrl implements ISharedDependencyBuilder {

        public BuilderByUrl() {
            dependencySide = DependencySide.Shared;
        }

        @Override
        @Deprecated
        public BuilderByUrl setDependencySide(DependencySide dependencySide) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BuilderByUrl setDependencyType(DependencyType dependencyType) {
            return (BuilderByUrl) super.setDependencyType(dependencyType);
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
        protected ElementSharedJarDependency buildDep(DependencyLord dependencyLord) throws Throwable {
            throw new NotImplementedException();
        }
    }

}
