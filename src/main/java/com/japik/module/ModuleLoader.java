package com.japik.module;

import com.japik.Japik;
import com.japik.dep.DependencyLord;
import com.japik.dep.ElementImplJarDependency;
import com.japik.dep.ModuleImplJarDependency;
import com.japik.dep.Tenant;
import com.japik.element.AElementLoader;
import com.japik.element.ElementType;
import com.japik.logger.ILogger;
import com.japik.service.IService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ModuleLoader extends AElementLoader<IModule<?>> {
    private final IService<?> service;
    private final Map<String, Map<String, String>> moduleNameSettingsMap = new HashMap<>();

    public ModuleLoader(Japik server, Path corePath, DependencyLord dependencyLord, ClassLoader baseClassLoader, ILogger logger,
                        IService<?> service) {
        super(ElementType.Module, server, corePath, dependencyLord, baseClassLoader, logger);
        this.service = service;
    }

    public void preloadModuleSettings(String moduleName, Map<String, String> settings){
        lock.lock();
        try {
            moduleNameSettingsMap.put(moduleName, settings);
        } finally {
            lock.unlock();
        }
    }

    public void removePreloadedModuleSettings(String moduleName){
        lock.lock();
        try{
            moduleNameSettingsMap.remove(moduleName);
        } finally {
            lock.unlock();
        }
    }

    // -------------

    @Override
    protected ModuleImplJarDependency.BuilderByType createDependencyBuilderByType(String elSubtype, String elName, @Nullable String elVersion) {
        final ModuleImplJarDependency.BuilderByType implDepBuilder =
                new ModuleImplJarDependency.BuilderByType(corePath);
        implDepBuilder
                .setElementSubtype(elSubtype)
                .setElementVersion(elVersion)
                .setElementName(elName);

        return implDepBuilder;
    }

    @Override
    protected ModuleImplJarDependency.BuilderByUrl createDependencyBuilderByUrl(URL elUrl, String elName) {
        final ModuleImplJarDependency.BuilderByUrl implDepBuilder =
                new ModuleImplJarDependency.BuilderByUrl();
        implDepBuilder
                .setUrl(elUrl)
                .setElementName(elName);

        return implDepBuilder;
    }

    @Override
    protected IModule<?> createElement(ElementImplJarDependency implDependency, String elName, Tenant elTenant) throws Throwable {

        //region load service class
        final Class<?> clazz = Class.forName(
                implDependency.getElementClassName(),
                true,
                implDependency.getClassLoader()
        );
        if (!IModule.class.isAssignableFrom(clazz))
            throw new IllegalClassFormatException("Is not assignable to IModule");
        final Constructor<?> ctor = clazz.getConstructor(
                ModuleParams.class
        );
        //endregion

        // region create element
        final IModule<?> element = (IModule<?>) ctor.newInstance(
                new ModuleParams(
                        service,
                        implDependency.getElementSubtype(),
                        elName,
                        logger,
                        elTenant
                )
        );
        //endregion

        // region put preloaded settings
        {
            final Map<String, String> preSettings = moduleNameSettingsMap.get(elName);
            if (preSettings != null) {
                for (final String key : preSettings.keySet()) {
                    element.getSettings().put(key, preSettings.get(key));
                }
                logger.info("Module settings preloaded. " + element.toString());
            }
        }
        // endregion

        return element;
    }

    public <MC extends IModuleConnection> IModule<MC> getModuleOrThrow(String moduleName) throws ModuleNotFoundException {
        final IModule<MC> element = (IModule<MC>) nameElementMap.get(moduleName);
        if (element == null) {
            throw new ModuleNotFoundException(moduleName, service.getName());
        }
        return element;
    }

    @RequiredArgsConstructor
    public static final class Builder{
        private final Japik server;
        private final Path corePath;
        private final DependencyLord dependencyLord;
        private final ClassLoader baseClassLoader;
        private final ILogger logger;

        public ModuleLoader build(IService<?> service){
            return new ModuleLoader(server, corePath, dependencyLord, baseClassLoader, logger, service);
        }
    }
}
