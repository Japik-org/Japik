package com.pro100kryto.server.tick;

import com.pro100kryto.server.dep.Tenant;
import com.pro100kryto.server.livecycle.EmptyLiveCycleImpl;
import com.pro100kryto.server.livecycle.ILiveCycle;
import com.pro100kryto.server.livecycle.ILiveCycleImpl;
import com.pro100kryto.server.livecycle.controller.LiveCycleController;
import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.settings.ISettingsManagerCallback;
import com.pro100kryto.server.settings.SettingsManager;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

public abstract class ATickGroup implements ITickCallback, ITickGroup, ISettingsManagerCallback {
    protected final ITickGroupCallback tickGroupCallback;
    protected final long id;
    protected final Tenant tenant;
    protected final ILogger logger;

    protected final SettingsManager settingsManager;
    protected final LiveCycleController liveCycleController;

    public ATickGroup(ITickGroupCallback tickGroupCallback, long id, Tenant tenant, ILogger logger,
                      @Nullable ReentrantLock liveCycleLock){
        this.tickGroupCallback = tickGroupCallback;
        this.id = id;
        this.tenant = tenant;
        this.logger = logger;

        settingsManager = new SettingsManager(this, logger);
        liveCycleController = new LiveCycleController.Builder()
                .setLock(liveCycleLock)
                .setDefaultImpl(getDefaultLiveCycleImpl())
                .setLogger(logger)
                .setElementName("TickGroup #"+id)
                .build();
    }

    @Override
    public final long getId() {
        return id;
    }

    @Override
    public final Tenant getTenant() {
        return tenant;
    }

    @Override
    public final ILiveCycle getLiveCycle() {
        return liveCycleController;
    }

    @Override
    public final SettingsManager getSettingsManager() {
        return settingsManager;
    }

    // virtual

    protected ILiveCycleImpl getDefaultLiveCycleImpl(){
        return EmptyLiveCycleImpl.instance;
    }
}
