package com.pro100kryto.server.service;

import org.jetbrains.annotations.Nullable;

public abstract class AServiceType <T extends IServiceConnection> {
    private T serviceConnection;
    protected final IServiceControl service;
    protected final IServiceTypeCallback callback;

    public AServiceType(Service service){
        this.service = service;
        callback = service;
    }

    public final void start() throws Throwable {
        beforeStart();
        serviceConnection = createServiceConnection();
        afterStart();
    }

    public final void stop() throws Throwable {
        beforeStop();
        serviceConnection = null;
        afterStop();
    }

    @Nullable
    public T getServiceConnection(){
        return serviceConnection;
    }

    protected void beforeStart() throws Throwable{}
    protected void afterStart() throws Throwable{}

    protected void beforeStop() throws Throwable{}
    protected void afterStop() throws Throwable{}

    @Nullable
    protected abstract T createServiceConnection() throws Throwable;
}
