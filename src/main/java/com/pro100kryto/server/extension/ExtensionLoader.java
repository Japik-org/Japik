package com.pro100kryto.server.extension;

import com.pro100kryto.server.Server;
import com.pro100kryto.server.dep.DependencyLord;
import com.pro100kryto.server.dep.ElementImplJarDependency;
import com.pro100kryto.server.dep.ExtensionImplJarDependency;
import com.pro100kryto.server.dep.Tenant;
import com.pro100kryto.server.element.AElementLoader;
import com.pro100kryto.server.element.ElementType;
import com.pro100kryto.server.logger.ILogger;
import org.jetbrains.annotations.Nullable;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Path;

public final class ExtensionLoader extends AElementLoader<IExtension<?>> {

    public ExtensionLoader(Server server, Path corePath, DependencyLord dependencyLord, ClassLoader baseClassLoader, ILogger logger) {
        super(ElementType.Extension, server, corePath, dependencyLord, baseClassLoader, logger);
    }

    @Override
    protected ExtensionImplJarDependency.BuilderByType createDependencyBuilderByType(String elSubtype, String elName, @Nullable String elVersion) {
        final ExtensionImplJarDependency.BuilderByType implDepBuilder =
                new ExtensionImplJarDependency.BuilderByType(corePath);
        implDepBuilder
                .setElementSubtype(elSubtype)
                .setElementVersion(elVersion)
                .setElementName(elName);

        return implDepBuilder;
    }

    @Override
    protected ExtensionImplJarDependency.BuilderByUrl createDependencyBuilderByUrl(URL elUrl, String elName) {
        final ExtensionImplJarDependency.BuilderByUrl implDepBuilder =
                new ExtensionImplJarDependency.BuilderByUrl();
        implDepBuilder
                .setUrl(elUrl)
                .setElementName(elName);

        return implDepBuilder;
    }

    @Override
    protected IExtension<?> createElement(ElementImplJarDependency implDependency, String elName, Tenant elTenant) throws Throwable {
        //region load service class
        final Class<?> clazz = Class.forName(
                implDependency.getElementClassName(),
                true,
                implDependency.getClassLoader()
        );
        if (!IExtension.class.isAssignableFrom(clazz))
            throw new IllegalClassFormatException("Is not assignable to IService");
        final Constructor<?> ctor = clazz.getConstructor(
                ExtensionParams.class
        );
        //endregion

        // region create element
        final IExtension<?> element = (IExtension<?>) ctor.newInstance(
                new ExtensionParams(
                        server,
                        implDependency.getSharedSubtype(),
                        elName,
                        elTenant,
                        logger
                )
        );
        //endregion

        return element;
    }

}
