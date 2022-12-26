package com.japik;

import com.japik.dep.DependencyLord;
import com.japik.extension.ExtensionLoader;
import com.japik.extension.IExtension;
import com.japik.livecycle.ILiveCycle;
import com.japik.livecycle.ILiveCycleImpl;
import com.japik.livecycle.controller.LiveCycleController;
import com.japik.logger.ILogger;
import com.japik.logger.LoggerManager;
import com.japik.logger.SystemOutLogger;
import com.japik.networking.IProtocol;
import com.japik.networking.LocalProtocol;
import com.japik.networking.Networking;
import com.japik.networking.Remote;
import com.japik.properties.ProjectProperties;
import com.japik.service.IService;
import com.japik.service.ServiceLoader;
import com.japik.settings.*;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public final class Japik implements ISettingsManagerCallback {
    @Getter
    private final Path workingPath;

    private final ClassLoader parentClassLoader;

    @Getter
    private final LiveCycleController liveCycle;

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

    @Getter
    private final Networking networking = new Networking(this);


    public Japik(Path workingPath) throws IOException {
        this.workingPath = workingPath;
        this.parentClassLoader = this.getClass().getClassLoader();
        final ServerLiveCycleImpl serverLiveCycle = new ServerLiveCycleImpl(this);
        liveCycle = new LiveCycleController.Builder()
                .setDefaultImpl(serverLiveCycle)
                .setElementName("Server")
                .setLogger(SystemOutLogger.instance)
                .build();

        mainLogger = SystemOutLogger.instance;
        liveCycle.setLogger(mainLogger);

        settings = new Settings();
        settingsManager = new SettingsManager(this, mainLogger, settings);

        projectProperties = new ProjectProperties();
        projectProperties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("project.properties"));
    }

    private final class ServerLiveCycleImpl implements ILiveCycleImpl {
        private final Japik server;

        private ServerLiveCycleImpl(Japik server) {
            this.server = server;
        }

        @Override
        public void init() throws Throwable {
            mainLogger = loggerManager.getMainLogger();
            liveCycle.setLogger(mainLogger);

            mainLogger.info("Server version is " + projectProperties.getVersion());

            serverClassLoader = new URLClassLoader(new URL[0], parentClassLoader);

            dependencyLord = new DependencyLord(server.workingPath);

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
                            liveCycle.setEnabledAutoFixBroken(val);
                        }
                    },
                    Boolean.toString(false)
            ));

            liveCycle.getInitImplQueue().putAutoPriorityOrder("init-local-protocol", () -> {
                final LocalProtocol localProtocol = new LocalProtocol(
                        server,
                        new Settings()
                );
                networking.getProtocolCollection().add(localProtocol);
                try{
                    localProtocol.getLiveCycle().init();
                } catch (Throwable throwable) {
                    networking.getProtocolCollection().remove(localProtocol);
                }
                mainLogger.info("LocalProtocol initialized.");
            });

            liveCycle.getInitImplQueue().putAutoPriorityOrder("init-local-remote", () -> {
                final Remote localRemote = networking.getRemoteCollection().add(
                        new Remote.Builder()
                                .setProtocolName(LocalProtocol.name)
                                .setRemoteName(LocalProtocol.name)
                );
                try {
                    localRemote.getLiveCycle().init();
                } catch (Throwable throwable) {
                    networking.getRemoteCollection().remove(localRemote);
                    throw throwable;
                }
                mainLogger.info("LocalRemote initialized.");
            });
        }

        @Override
        public void start() throws Throwable {
            liveCycle.getStartImplQueue().putAutoPriorityOrder("start-local-protocol", () -> {
                networking.getProtocolCollection().getByName(LocalProtocol.name).getLiveCycle().start();
            });
            liveCycle.getStartImplQueue().putAutoPriorityOrder("start-local-remote", () -> {
                networking.getRemoteCollection().getByName(LocalProtocol.name).getLiveCycle().start();
            });
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

            liveCycle.getStartImplQueue().putAutoPriorityOrder("stopSlow-local-remote", () -> {
                networking.getRemoteCollection().getByName(LocalProtocol.name).getLiveCycle().stopSlow();
            });
            liveCycle.getStartImplQueue().putAutoPriorityOrder("stopSlow-local-protocol", () -> {
                networking.getProtocolCollection().getByName(LocalProtocol.name).getLiveCycle().stopSlow();
            });
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

            liveCycle.getStartImplQueue().putAutoPriorityOrder("stopForce-local-remote", () -> {
                networking.getRemoteCollection().getByName(LocalProtocol.name).getLiveCycle().stopForce();
            });
            liveCycle.getStartImplQueue().putAutoPriorityOrder("stopForce-local-protocol", () -> {
                networking.getProtocolCollection().getByName(LocalProtocol.name).getLiveCycle().stopForce();
            });
        }

        @Override
        public void destroy() {
            for (final Remote remote : networking.getRemoteCollection()) {
                if (remote.getLiveCycle().getStatus().isInitialized())
                    remote.getLiveCycle().destroy();
            }
            networking.getRemoteCollection().clear();

            for (final IProtocol protocol: networking.getProtocolCollection()) {
                if (protocol.getLiveCycle().getStatus().isInitialized())
                    protocol.getLiveCycle().destroy();
            }
            networking.getProtocolCollection().clear();

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
            liveCycle.setLogger(mainLogger);
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
