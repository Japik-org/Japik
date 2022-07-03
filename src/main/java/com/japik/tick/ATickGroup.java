package com.japik.tick;

import com.japik.dep.Tenant;
import com.japik.livecycle.EmptyLiveCycleImpl;
import com.japik.livecycle.ILiveCycle;
import com.japik.livecycle.ILiveCycleImpl;
import com.japik.livecycle.controller.LiveCycleController;
import com.japik.logger.ILogger;
import com.japik.settings.ISettingsManagerCallback;
import com.japik.settings.SettingsManager;
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
