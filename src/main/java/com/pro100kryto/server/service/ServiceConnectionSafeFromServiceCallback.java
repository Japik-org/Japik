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
            final IService<SC> service = serviceCallback.getServiceConnection(serviceName);
            if (service == null) throw new ServiceNotFoundException(serviceName);

            final SC oldSC = serviceConnection;
            final SC newSC = service.getServiceConnection();
            if (oldSC != newSC && oldSC!=null && !oldSC.isClosed()){
                oldSC.close();
            }
            serviceConnection = newSC;

            serviceConnection.ping();

        } finally {
            refreshLock.unlock();
        }
    }
}
