package com.japik.dep;

import com.google.common.base.Strings;
import com.japik.element.ElementType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Predicate;

@Getter
public abstract class ElementImplJarDependency extends ElementJarDependency implements IImplDependency {

    protected final String elementClassName;
    @Nullable
    protected final String sharedSubtype;

    @Nullable
    protected final ISharedDependency sharedDependency;

    private final boolean importAllPackages;

    protected ElementImplJarDependency(DependencyLord callback, BuilderByType builder) {
        super(callback, builder);
        elementClassName = builder.getElementClassName();
        sharedSubtype = builder.getSharedSubtype();
        sharedDependency = builder.getSharedDependency();
        importAllPackages = builder.isImportAllPackages();
    }

    protected ElementImplJarDependency(DependencyLord callback, BuilderByUrl builder) {
        super(callback, builder);
        elementClassName = builder.getElementClassName();
        sharedSubtype = builder.getSharedSubtype();
        sharedDependency = builder.getSharedDependency();
        importAllPackages = builder.isImportAllPackages();;
    }

    @Override
    protected void addDependencies(ArrayList<IDependency> dependencyListOut) throws Throwable {
        super.addDependencies(dependencyListOut);
        if (sharedDependency != null) {
            dependencyListOut.add(sharedDependency);
        }
    }

    @Override
    protected @Nullable Predicate<String> getClassNameFilter() {
        final String pkg = basePackage+"."+
                elementType.toString().toLowerCase()+"s.";

        return s -> (importAllPackages || s.startsWith(pkg+elementSubtype.toLowerCase())) &&
                !s.startsWith(pkg + elementSubtype.toLowerCase() + ".connection") &&
                !s.startsWith(pkg + elementSubtype.toLowerCase() + ".shared");
    }

    public Class<?> createElementClass() throws ClassNotFoundException {
        if (getStatus() != DependencyStatus.RESOLVED) {
            throw new IllegalStateException("Dependency should be resolved before create class");
        }
        if (classLoader == null) {
            throw new IllegalStateException("ClassLoader not initialized");
        }
        return Class.forName(
                elementClassName,
                true,
                classLoader
        );
    }

    // --------------------

    @Getter
    @Accessors(chain = true)
    public abstract static class BuilderByType extends ElementJarDependency.BuilderByType implements IImplDependencyBuilder {
        protected String elementClassName;
        @Nullable
        protected String sharedSubtype;

        @Nullable
        protected ElementSharedJarDependency.BuilderByType sharedDependencyBuilder;
        @Nullable
        protected ISharedDependency sharedDependency;

        private boolean importAllPackages;

        public BuilderByType(@NotNull Path corePath) {
            super(corePath);
            dependencySide = DependencySide.Impl;
        }

        @Override
        @Deprecated
        public BuilderByType setDependencySide(DependencySide dependencySide) {
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
        public BuilderByType setElementType(ElementType elementType) {
            return (BuilderByType) super.setElementType(elementType);
        }

        @Override
        public BuilderByType setTenant(Tenant tenant) {
            return (BuilderByType) super.setTenant(tenant);
        }

        @Override
        public BuilderByType setBaseClassLoader(@Nullable ClassLoader baseClassLoader) {
            return (BuilderByType) super.setBaseClassLoader(baseClassLoader);
        }

        @Override
        public BuilderByType setElementName(@Nullable String elementName) {
            return (BuilderByType) super.setElementName(elementName);
        }

        protected final void initImplType() {
            elementClassName = attributes.getValue("Class");
            if (Strings.isNullOrEmpty(elementClassName)) {
                elementClassName = basePackage+"."+ // com.ex.server.
                        elementType.toString().toLowerCase()+"s."+ // services.
                        elementSubtype.toLowerCase()+"."+ // x.
                        elementSubtype+elementType; // XService
            }
            sharedSubtype = attributes.getValue("Shared-Subtype");

            importAllPackages = "true".equals(attributes.getValue("Import-All-Packages"));
        }

        protected abstract ElementSharedJarDependency.BuilderByType createSharedDepBuilder();

        @Override
        public void init() throws IOException {
            super.init();
            initImplType();
            sharedDependencyBuilder = createSharedDepBuilder();
        }

        @Nullable
        public final ISharedDependency rentSharedDep(DependencyLord dependencyLord) throws Throwable {
            if (sharedSubtype != null && sharedDependencyBuilder != null) {
                return  (ISharedDependency) dependencyLord.rentShared(sharedDependencyBuilder);
            }

            return null;
        }

        @Override
        protected final ElementImplJarDependency buildDep(DependencyLord dependencyLord) throws Throwable {
            sharedDependency = rentSharedDep(dependencyLord);
            return buildImplDep(dependencyLord);
        }

        protected abstract ElementImplJarDependency buildImplDep(DependencyLord dependencyLord) throws Throwable;
    }

    @Getter
    @Accessors(chain = true)
    public abstract static class BuilderByUrl extends ElementJarDependency.BuilderByUrl implements IImplDependencyBuilder {
        @Setter
        protected Path corePath;
        protected String elementClassName;
        @Nullable
        protected String sharedSubtype;

        @Setter
        @Nullable
        protected ClassLoader baseClassLoader;

        @Nullable
        protected ISharedDependencyBuilder sharedDependencyBuilder;
        @Nullable
        protected ISharedDependency sharedDependency;

        private boolean importAllPackages;

        public BuilderByUrl() {
            dependencySide = DependencySide.Impl;
        }

        @Override
        @Deprecated
        public BuilderByUrl setDependencySide(DependencySide dependencySide) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BuilderByUrl setElementType(ElementType elementType) {
            return (BuilderByUrl) super.setElementType(elementType);
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
        public BuilderByUrl setElementName(String elementName) {
            return (BuilderByUrl) super.setElementName(elementName);
        }

        @Override
        public BuilderByUrl setUrl(URL url) {
            return (BuilderByUrl) super.setUrl(url);
        }

        @Override
        protected boolean isNotValid() {
            return super.isNotValid() || corePath == null;
        }

        protected final void initImplType() {
            elementClassName = attributes.getValue("Class");
            if (Strings.isNullOrEmpty(elementClassName)) {
                elementClassName = basePackage+"."+ // com.ex.server.
                        elementType.toString().toLowerCase()+"s."+ // services.
                        elementSubtype.toLowerCase()+"."+ // x.
                        elementSubtype+elementType; // XService
            }
            sharedSubtype = attributes.getValue("Shared-Subtype");

            importAllPackages = "true".equals(attributes.getValue("Import-All-Packages"));
        }

        protected abstract ISharedDependencyBuilder createSharedDepBuilder();

        @Override
        public void init() throws IOException {
            super.init();
            initImplType();
            sharedDependencyBuilder = createSharedDepBuilder();
        }

        @Nullable
        public final ISharedDependency buildSharedDep(DependencyLord dependencyLord) throws Throwable {
            if (sharedSubtype != null && sharedDependencyBuilder != null) {
                return (ISharedDependency) dependencyLord.rentShared(sharedDependencyBuilder);
            }

            return null;
        }

        @Override
        protected final ElementImplJarDependency buildDep(DependencyLord dependencyLord) throws Throwable {
            sharedDependency = buildSharedDep(dependencyLord);
            return buildImplDep(dependencyLord);
        }

        protected abstract ElementImplJarDependency buildImplDep(DependencyLord dependencyLord) throws Throwable;
    }
}
