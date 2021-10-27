package com.pro100kryto.server.service;

import com.pro100kryto.server.logger.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AServiceConnection<S extends IService<SC>, SC extends IServiceConnection> implements IServiceConnection{
    @Nullable
    private S service;
    protected final ILogger logger;
    protected final String serviceName;
    protected final String serviceType;
    private boolean isClosed = false;

    public AServiceConnection(@NotNull S service, ILogger logger) {
        this.service = Objects.requireNonNull(service);
        this.logger = Objects.requireNonNull(logger);
        this.serviceName = service.getName();
        this.serviceType = service.getType();
    }

    @Override
    public final String getServiceName() {
        return serviceName;
    }

    @Override
    public final String getServiceType() {
        return serviceType;
    }

    @Override
    public synchronized final void close() {
        if (isClosed) {
            throw new IllegalStateException();
        }
        isClosed = true;
        onClose();
        service = null;
    }

    @Override
    public final boolean isClosed() {
        return isClosed;
    }

    @Nullable
    protected final S getService(){
        return service;
    }

    // virtual

    @Override
    public synchronized boolean isAliveService() {
        if (isClosed){
            throw new IllegalStateException();
        }
        return service.getLiveCycle().getStatus().isStarted();
    }

    @Override
    public boolean ping() {
        return true;
    }

    protected void onClose(){}

}
