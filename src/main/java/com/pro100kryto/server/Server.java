package com.pro100kryto.server;

import com.pro100kryto.server.dep.DependencyLord;
import com.pro100kryto.server.extension.ExtensionLoader;
import com.pro100kryto.server.extension.IExtension;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.livecycle.ILiveCycleImpl;
import com.pro100kryto.server.livecycle.controller.LiveCycleController;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.logger.LoggerManager;
import com.pro100kryto.server.logger.SystemOutLogger;
import com.pro100kryto.server.properties.ProjectProperties;
import com.pro100kryto.server.service.IService;
import com.pro100kryto.server.service.ServiceLoader;
import com.pro100kryto.server.settings.*;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Server implements ISettingsManagerCallback {
    @Getter
    private final Path workingPath;

    private final ClassLoader parentClassLoader;
    private final LiveCycleController liveCycleController;

    @Getter
    private final ProjectProperties projectProperties;
    @Getter
    private final LoggerManager loggerManager = new LoggerManager();

    private ILogger mainLogger;
    @Nullable
    private DependencyLord dependencyLord;
    @Getter @Nullable
    private ServiceLoader serviceLoader;
    @Getter @Nullable
    private ExtensionLoader extensionLoader;
    @Getter
    private final Settings settings;
    @Getter
    private final SettingsManager settingsManager;
    @Nullable
    private URLClassLoader serverClassLoader;


    public Server(Path workingPath) throws IOException {
        this.workingPath = workingPath;
        this.parentClassLoader = this.getClass().getClassLoader();
        final ServerLiveCycleImpl serverLiveCycle = new ServerLiveCycleImpl(this);
        liveCycleController = new LiveCycleController.Builder()
                .setDefaultImpl(serverLiveCycle)
                .setElementName("Server")
                .setLogger(SystemOutLogger.instance)
                .build();

        mainLogger = SystemOutLogger.instance;
        liveCycleController.setLogger(mainLogger);

        settings = new Settings();
        settingsManager = new SettingsManager(settings, this, mainLogger);

        projectProperties = new ProjectProperties();
        projectProperties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
    }

    // ------- live cycle

    public ILiveCycle getLiveCycle(){
        return liveCycleController;
    }

    private final class ServerLiveCycleImpl implements ILiveCycleImpl {
        private final Server server;

        private ServerLiveCycleImpl(Server server) {
            this.server = server;
        }

        @Override
        public void init() throws Throwable {
            mainLogger = loggerManager.getMainLogger();
            liveCycleController.setLogger(mainLogger);

            mainLogger.info("Server version is " + projectProperties.getVersion());

            serverClassLoader = new URLClassLoader(new URL[0], parentClassLoader);

            dependencyLord = new DependencyLord(
                    Paths.get(server.getWorkingPath().toString(), "core")
            );

            serviceLoader = new ServiceLoader(
                    server,
                    dependencyLord.getCorePath(),
                    dependencyLord,
                    serverClassLoader,
                    mainLogger
            );
            extensionLoader = new ExtensionLoader(
                    server,
                    dependencyLord.getCorePath(),
                    dependencyLord,
                    serverClassLoader,
                    mainLogger
            );

            settingsManager.setLogger(mainLogger);
            settingsManager.setListener(new SettingListenerContainer(
                    BaseServerSettings.KEY_AUTO_FIX_BROKEN_ENABLE,
                    new BooleanSettingListener() {
                        @Override
                        public void apply2(String key, Boolean val, SettingListenerEventMask eventMask) {
                            liveCycleController.setEnabledAutoFixBroken(val);
                        }
                    },
                    Boolean.toString(false)
            ));
        }

        @Override
        public void start() throws Throwable {
        }

        @Override
        public void stopSlow() throws Throwable {
            for (final IService<?> service: serviceLoader.getElements()){
                try {
                    service.getLiveCycle().announceStop();
                } catch (IllegalStateException illegalStateException){
                    mainLogger.exception(illegalStateException);
                }
            }

            for (final IExtension<?> ext: extensionLoader.getElements()){
                try {
                    ext.getLiveCycle().announceStop();
                } catch (IllegalStateException illegalStateException){
                    mainLogger.exception(illegalStateException);
                }
            }

            // ---

            for (final IService<?> service: serviceLoader.getElements()){
                try {
                    service.getLiveCycle().stopSlow();
                } catch (IllegalStateException illegalStateException){
                    mainLogger.exception(illegalStateException);
                }
            }

            for (final IExtension<?> ext: extensionLoader.getElements()){
                try {
                    ext.getLiveCycle().stopSlow();
                } catch (IllegalStateException illegalStateException){
                    mainLogger.exception(illegalStateException);
                }
            }
        }

        @Override
        public void stopForce() {
            serviceLoader.getElements().forEach( (service) -> {
                try {
                    if (service.getLiveCycle().getStatus().isStarted() || service.getLiveCycle().getStatus().isBroken()) {
                        service.getLiveCycle().stopForce();
                    }
                } catch (IllegalStateException illegalStateException){
                    mainLogger.exception(illegalStateException);
                }
            });

            extensionLoader.getElements().forEach( (ext) -> {
                try {
                    if (ext.getLiveCycle().getStatus().isStarted() || ext.getLiveCycle().getStatus().isBroken()) {
                        ext.getLiveCycle().stopForce();
                    }
                } catch (IllegalStateException illegalStateException){
                    mainLogger.exception(illegalStateException);
                }
            });
        }

        @Override
        public void destroy() {
            settingsManager.removeAllListeners();

            if (serviceLoader != null) {
                serviceLoader.unloadAll();
            }
            serviceLoader = null;

            if (extensionLoader != null) {
                extensionLoader.unloadAll();
            }
            extensionLoader = null;

            mainLogger = SystemOutLogger.instance;
            liveCycleController.setLogger(mainLogger);
            loggerManager.unregisterLoggers();

            if (dependencyLord != null) {
                dependencyLord.releaseAll();
            }
            dependencyLord = null;

            if (serverClassLoader != null) {
                try {
                    serverClassLoader.close();
                } catch (IOException e) {
                    try {
                        mainLogger.exception(e);
                    } catch (Throwable ignored){
                    }
                }
            }
            serverClassLoader = null;
        }

        @Override
        public boolean canBeStoppedSafe() {
            for (final IService<?> service: serviceLoader.getElements()){
                if (!service.getLiveCycle().canBeStoppedSafe()) return false;
            }

            for (final IExtension<?> extension: extensionLoader.getElements()){
                if (!extension.getLiveCycle().canBeStoppedSafe()) return false;
            }

            return true;
        }

        @Override
        public void announceStop() {
            for (final IService<?> service: serviceLoader.getElements()){
                service.getLiveCycle().announceStop();
            }

            for (final IExtension<?> extension: extensionLoader.getElements()){
                extension.getLiveCycle().announceStop();
            }
        }
    }
}
