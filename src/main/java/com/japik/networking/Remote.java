package com.japik.networking;

import com.japik.Japik;
import com.japik.livecycle.ILiveCycleImpl;
import com.japik.livecycle.controller.LiveCycleController;
import com.japik.logger.ILogger;
import com.japik.settings.ISettingsManagerCallback;
import com.japik.settings.Settings;
import com.japik.settings.SettingsManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
public final class Remote implements ISettingsManagerCallback {
    private final ProtocolCollection protocolManager;
    private final String name;
    private final String protocolName;
    private final Settings protocolSettings;
    private final SettingsManager protocolSettingsManager;
    private final ILogger logger;
    private final LiveCycleController liveCycle;

    @Nullable
    private IProtocol protocol;
    @Nullable
    private IProtocolInstance protocolInstance;

    public Remote(ProtocolCollection protocolManager,
                  String name,
                  String protocolName, Settings protocolSettings,
                  ILogger logger) {
        this.protocolManager = protocolManager;
        this.name = name;
        this.protocolName = protocolName;
        this.protocolSettings = protocolSettings;
        this.logger = logger;
        this.protocolSettingsManager = new SettingsManager(this, logger, protocolSettings);
        this.liveCycle = new LiveCycleController.Builder()
                .setElementName("Remote '"+this.name+"'")
                .setDefaultImpl(new RemoteLiveCycle())
                .setLogger(logger)
                .build();
    }

    private final class RemoteLiveCycle implements ILiveCycleImpl {

        @Override
        public void init() throws Throwable {
            protocol = protocolManager.getByName(protocolName);
            if (protocol == null) {
                throw new NullPointerException("Protocol '"+protocolName+"' not found.");
            }
        }

        @Override
        public void start() throws Throwable {
            protocolInstance = protocol.newInstance(protocolSettingsManager.getSettings());
        }

        @Override
        public void stopSlow() throws Throwable {
            protocolInstance.close();
            protocolInstance = null;
        }

        @Override
        public void stopForce() {
            try {
                protocolInstance.close();
            } catch (Exception e) {
                logger.exception(e, "Failed close protocol instance '"+protocolName+"'.");
            }
            protocolInstance = null;
        }

        @Override
        public void destroy() {
            protocol = null;
        }

        @Override
        public void announceStop(){
        }

        @Override
        public boolean canBeStoppedSafe() {
            return true;
        }
    }


    @Getter @Setter
    public static final class Builder {
        private String remoteName;
        private String protocolName;
        private Settings protocolSettings;
        @Nullable
        private ILogger logger;

        public Remote build(Japik server) {
            if (logger == null) {
                logger = server.getLoggerManager().getMainLogger();
            }

            return new Remote(
                    server.getNetworking().getProtocolCollection(),
                    remoteName,
                    protocolName,
                    protocolSettings,
                    logger
            );
        }
    }
}
