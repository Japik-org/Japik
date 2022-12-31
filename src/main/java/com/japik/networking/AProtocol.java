package com.japik.networking;

import com.japik.Japik;
import com.japik.livecycle.EmptyLiveCycleImpl;
import com.japik.livecycle.controller.LiveCycleController;
import com.japik.logger.ILogger;
import com.japik.settings.ISettingsManagerCallback;
import com.japik.settings.Settings;
import com.japik.settings.SettingsManager;
import lombok.Getter;

import java.util.concurrent.locks.ReentrantLock;

@Getter
public abstract class AProtocol implements IProtocol, ISettingsManagerCallback {
    protected final String name;
    protected final Japik server;
    protected final ILogger logger;
    protected final Settings settings;
    protected final SettingsManager settingsManager;
    protected final LiveCycleController liveCycle;

    public AProtocol(String name, Japik server, Settings settings) {
        this.name = name.toLowerCase();
        this.server = server;
        this.logger = server.getLoggerManager().getOrCreateLogger(name+"Protocol");
        this.settings = settings;
        this.settingsManager = new SettingsManager(
                this,
                logger,
                settings
        );
        this.liveCycle = new LiveCycleController(
                logger,
                logger.getName(),
                new AProtocolLiveCycleImpl(),
                new ReentrantLock()
        );
    }

    protected void initLiveCycleController(LiveCycleController liveCycle) {
    }

    @Override
    public final IProtocolInstance newInstance(Settings protocolSettings) throws Exception {
        if (!liveCycle.getStatus().isStarted()){
            throw new IllegalStateException("Protocol '"+name+"' is not started.");
        }
        return newInstanceImpl(protocolSettings);
    }
    protected abstract IProtocolInstance newInstanceImpl(Settings protocolSettings) throws Exception;

    private final class AProtocolLiveCycleImpl extends EmptyLiveCycleImpl {
        @Override
        public void init() {
            initLiveCycleController(liveCycle);
        }
    }
}
