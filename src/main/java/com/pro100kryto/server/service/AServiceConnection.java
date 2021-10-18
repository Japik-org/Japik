package com.pro100kryto.server.service;

import com.pro100kryto.server.logger.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AServiceConnection <S extends IService<SC>, SC extends IServiceConnection> implements IServiceConnection{
    @Nullable
    private S service;
    protected final ILogger logger;
    protected final String serviceType;
    protected final String serviceName;
    protected boolean isClosed = false;

    public AServiceConnection(@NotNull S service, ILogger logger, String serviceType, String serviceName) {
        this.service = Objects.requireNonNull(service);
        this.logger = Objects.requireNonNull(logger);
        this.serviceType = serviceType;
        this.serviceName = serviceName;
    }

    @Override
    public final String getServiceType() {
        return serviceType;
    }

    @Override
    public final String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean ping() {
        return true;
    }

    @Override
    public boolean isAliveService() {
        if (isClosed) throw new IllegalStateException();
        return service.getLiveCycle().getStatus().isStarted();
    }

    @Override
    public final void close() {
        if (isClosed) throw new IllegalStateException();
        isClosed = true;
        onClose();
        service = null;
    }

    @Override
    public final boolean isClosed() {
        return isClosed;
    }

    protected abstract void onClose();

    @Nullable
    protected S getService(){
        return service;
    }
}
