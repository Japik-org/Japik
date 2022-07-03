package com.japik.dep;

import com.google.common.reflect.ClassPath;
import com.japik.NotImplementedException;
import com.japik.element.ElementType;
import com.japik.utils.UtilsInternal;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

@Getter
public abstract class ElementJarDependency extends JarDependency {
    @NonNull
    protected final ElementType elementType;
    protected final String elementVersion;
    protected final String basePackage;

    protected final String elementCompleteType;
    protected final String elementSubtype;

    protected ElementJarDependency(DependencyLord callback, AElementJarDepBuilder builder) {
        super(callback, builder);
        elementType = builder.getElementType();
        elementVersion = builder.getElementVersion();
        basePackage = builder.getBasePackage();
        elementCompleteType = builder.getElementCompleteType();
        elementSubtype = builder.getElementSubtype();
    }

    @Override
    protected void addDependencies(ArrayList<IDependency> dependencyListOut) throws Throwable {
        super.addDependencies(dependencyListOut);

        try (final JarFile jarFile = new JarFile( file )) {

            final Attributes attributes = jarFile.getManifest().getAttributes(elementCompleteType);

            //region connect shared
            UtilsInternal.iterateAttributeValues(attributes, "Service-Shared-Subtype", (val) -> {
                final String connServiceType = val.split("-")[0];
                final String connServiceVersion = (val.contains("-v") ? val.split("-v")[1] : null);
                final ServiceSharedJarDependency.BuilderByType depBuilder =
                        new ServiceSharedJarDependency.BuilderByType(callback.getCorePath());

                depBuilder
                        .setBaseClassLoader(baseClassLoader)
                        .setElementSubtype(connServiceType)
                        .setElementVersion(connServiceVersion)
                        .setTenant(getTenantList().get(0));

                try {
                    final IDependency dependency = callback.rentShared(depBuilder);
                    dependencyListOut.add(dependency);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            UtilsInternal.iterateAttributeValues(attributes, "Service-Shared-Jar", (val) -> {
                final Path path = Paths.get(
                        callback.getCorePath().toString(),
                        "services",
                        val
                ).normalize();
                try {
                    final ServiceSharedJarDependency.BuilderByUrl depBuilder =
                            new ServiceSharedJarDependency.BuilderByUrl();

                    depBuilder
                            .setBaseClassLoader(baseClassLoader)
                            .setUrl(path.toUri().toURL())
                            .setTenant(getTenantList().get(0));

                    final IDependency dependency = callback.rentShared(depBuilder);
                    dependencyListOut.add(dependency);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            UtilsInternal.iterateAttributeValues(attributes, "Module-Shared-Subtype", (val) -> {
                final String connModuleType = val.split("-")[0];
                final String connModuleVersion = (val.contains("-v") ? val.split("-v")[1] : null);
                final ModuleSharedJarDependency.BuilderByType depBuilder =
                        new ModuleSharedJarDependency.BuilderByType(callback.getCorePath());
                depBuilder
                        .setBaseClassLoader(baseClassLoader)
                        .setElementSubtype(connModuleType)
                        .setElementVersion(connModuleVersion)
                        .setTenant(getTenantList().get(0));
                try {
                    final IDependency dependency = callback.rentShared(depBuilder);
                    dependencyListOut.add(dependency);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            UtilsInternal.iterateAttributeValues(attributes, "Module-Shared-Jar", (val) -> {
                final Path path = Paths.get(
                        callback.getCorePath().toString(),
                        "modules",
                        val
                ).normalize();
                try {
                    final ModuleSharedJarDependency.BuilderByUrl depBuilder =
                            new ModuleSharedJarDependency.BuilderByUrl();

                    depBuilder
                            .setBaseClassLoader(baseClassLoader)
                            .setUrl(path.toUri().toURL())
                            .setTenant(getTenantList().get(0));

                    final IDependency dependency = callback.rentShared(depBuilder);
                    dependencyListOut.add(dependency);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });

            UtilsInternal.iterateAttributeValues(attributes, "Lib-Shared-Jar", (val) -> {
                final Path path = Paths.get(
                        callback.getCorePath().toString(),
                        "libs",
                        val
                ).normalize();
                try {
                    final LibJarDependency.BuilderByUrl depBuilder = new LibJarDependency.BuilderByUrl();

                    depBuilder
                            //.setParentClassLoaderSupplier(() -> classLoader)
                            .setBaseClassLoader(baseClassLoader)
                            .setUrl(path.toUri().toURL())
                            .setTenant(getTenantList().get(0))
                            .setDependencySide(DependencySide.Shared);

                    final IDependency dependency = callback.rentShared(depBuilder);
                    dependencyListOut.add(dependency);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
            //endregion

            // region connect impl
            UtilsInternal.iterateAttributeValues(attributes, "Lib-Impl-Jar", (val) -> {
                final Path path = Paths.get(
                        callback.getCorePath().toString(),
                        "libs",
                        val
                ).normalize();
                try {
                    final LibJarDependency.BuilderByUrl depBuilder = new LibJarDependency.BuilderByUrl();

                    depBuilder
                            .setParentClassLoaderSupplier(() -> classLoader)
                            .setBaseClassLoader(baseClassLoader)
                            .setUrl(path.toUri().toURL())
                            .setTenant(getTenantList().get(0))
                            .setDependencySide(DependencySide.Impl);

                    final IDependency dependency = callback.buildImpl(depBuilder);
                    //implDepList.add((ILibDependency) dependency);
                    dependencyListOut.add(dependency);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
            //endregion

        }
    }

    @Override
    protected void loadClasses(ClassLoader classLoader) throws IOException {
        final URLClassLoader tempCL = new URLClassLoader(new URL[]{url}, null);
        tempCL.close();

        ClassPath.from(tempCL)
                .getAllClasses()
                .stream()
                .map(ClassPath.ClassInfo::getName)
                .filter(getClassNameFilter())
                .forEach(name -> {
                    try {
                        classLoader.loadClass(name);
                    } catch (ClassNotFoundException ignored) {
                    }
                });
    }

    // -------------

    @Getter
    @Accessors(chain = true)
    private abstract static class AElementJarDepBuilder extends JarDependency.BuilderByUrl {
        @Setter
        protected ElementType elementType;
        @Setter @Nullable
        protected String elementName;

        protected Attributes attributes;
        @Nullable
        protected String elementVersion;
        protected String basePackage;
        protected String elementCompleteType;
        protected String elementSubtype;

        protected final void initAttributes() throws IOException {
            try (final JarFile jarFile = new JarFile(file)) {
                elementCompleteType = elementType + "-" + dependencySide;

                attributes = jarFile.getManifest().getAttributes(elementCompleteType);
                if (attributes == null) {
                    throw new IllegalStateException("This element is not marked as '"+elementCompleteType+"'");
                }

                elementSubtype = Objects.requireNonNull(attributes.getValue("Subtype"));
                elementVersion = attributes.getValue("Version");
                basePackage = Objects.requireNonNull(attributes.getValue("Base-Package"));
            }
        }

        protected final void initTenant() {
            tenant = UtilsInternal.createElementTenant(elementType, elementSubtype, elementName);
        }

        @Override
        public void init() throws IOException {
            super.init(); // file
            initAttributes();
            if (tenant == null) initTenant();
        }
    }

    @Getter
    @Accessors(chain = true)
    public abstract static class BuilderByType extends AElementJarDepBuilder {
        @NonNull
        protected final Path corePath;

        public BuilderByType(@NotNull Path corePath) {
            this.corePath = corePath;
        }

        @Override
        @Deprecated
        public BuilderByType setDependencyType(DependencyType dependencyType) {
            elementType = ElementType.valueOf(dependencyType.name());
            return (BuilderByType) super.setDependencyType(dependencyType);
        }

        public BuilderByType setElementType(ElementType elementType) {
            this.elementType = elementType;
            super.setDependencyType(DependencyType.valueOf(elementType.name()));
            return this;
        }

        public BuilderByType setElementSubtype(String elementSubtype) {
            this.elementSubtype = elementSubtype;
            return this;
        }

        public BuilderByType setElementVersion(String elementVersion) {
            this.elementVersion = elementVersion;
            return this;
        }

        @Override
        public BuilderByType setElementName(@Nullable String elementName) {
            return (BuilderByType) super.setElementName(elementName);
        }

        @Override
        public boolean isNotValid() {
            return dependencyType == null || dependencySide == null || (tenant == null && elementName == null) || elementType == null || elementSubtype == null;
        }

        public URL buildURL() throws IOException {
            return UtilsInternal.findElementPath(
                    elementType,
                    dependencySide,
                    elementSubtype,
                    (elementVersion != null ? elementVersion.split("\\.")[0] : null),
                    corePath
            ).toUri().toURL();
        }

        protected final void initURL() throws IOException {
            url = buildURL();
        }

        @Override
        public void init() throws IOException {
            if (url == null) initURL();
            super.init(); // file, attributes, tenant
        }

        @Override
        @Deprecated
        public final BuilderByType setUrl(URL url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BuilderByType setDependencySide(DependencySide dependencySide) {
            return (BuilderByType) super.setDependencySide(dependencySide);
        }

        @Override
        public BuilderByType setTenant(Tenant tenant) {
            return (BuilderByType) super.setTenant(tenant);
        }

        @Override
        protected ElementJarDependency buildDep(DependencyLord dependencyLord) throws Throwable {
            throw new NotImplementedException();
        }
    }

    @Getter
    @Accessors(chain = true)
    public abstract static class BuilderByUrl extends AElementJarDepBuilder {
        @Setter
        protected ElementType elementType;

        public BuilderByUrl() {
        }

        @Override
        protected boolean isNotValid() {
            return locationType == null || dependencyType == null || dependencySide == null || elementType == null;
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
        public BuilderByUrl setUrl(URL url) {
            return (BuilderByUrl) super.setUrl(url);
        }

        @Override
        protected ElementJarDependency buildDep(DependencyLord dependencyLord) throws Throwable {
            throw new NotImplementedException();
        }
    }
}
