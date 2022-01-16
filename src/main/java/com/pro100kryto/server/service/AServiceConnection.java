package com.pro100kryto.server.service;

import com.pro100kryto.server.logger.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AServiceConnection<S extends IService<SC>, SC extends IServiceConnection>
        implements IServiceConnection{

    @Nullable
    protected S service;
    protected final IServiceConnectionCallback callback;
    protected final int id;
    protected final String serviceName;
    protected final String serviceType;
    protected final ILogger logger;

    private boolean isClosed = false;

    public AServiceConnection(@NotNull S service, ServiceConnectionParams params) {
        this.service = Objects.requireNonNull(service);
        this.callback = Objects.requireNonNull(params.getCallback());
        this.id = params.getId();
        this.serviceName = service.getName();
        this.serviceType = service.getType();
        this.logger = Objects.requireNonNull(params.getLogger());
    }

    @Nullable
    protected final S getService(){
        return service;
    }

    @Override
    public final int getId() {
        return id;
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

        callback.onCloseServiceConnection(id);
    }

    @Override
    public final boolean isClosed() {
        return isClosed;
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
