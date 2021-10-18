package com.pro100kryto.server.service;

import lombok.SneakyThrows;

public final class ServiceConnectionSafeFromServiceCallback <SC extends IServiceConnection> extends AServiceConnectionSafe<SC> {
    private final IServiceCallback serviceCallback;


    public ServiceConnectionSafeFromServiceCallback(IServiceCallback serviceCallback, String serviceName) {
        super(serviceName);
        this.serviceCallback = serviceCallback;
    }

    @SneakyThrows
    @Override
    public SC getServiceConnection() throws ServiceNotFoundException {
        return super.getServiceConnection();
    }

    /**
     * @throws ClassCastException - wrong service type
     */
    @Override
    public void refreshConnection() throws ServiceNotFoundException {
        if (isClosed) throw new IllegalStateException();
        refreshLock.lock();
        try{
            // !! ClassCastException !!
            final IService<SC> service = serviceCallback.createServiceConnection(serviceName);
            if (service == null) throw new ServiceNotFoundException(serviceName);
            serviceConnection = service.createServiceConnection();
            serviceConnection.ping();

        } finally {
            refreshLock.unlock();
        }
    }
}
