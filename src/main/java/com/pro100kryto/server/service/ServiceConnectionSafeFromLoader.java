package com.pro100kryto.server.service;

import lombok.SneakyThrows;

public final class ServiceConnectionSafeFromLoader <SC extends IServiceConnection> extends AServiceConnectionSafe<SC> {
    private final ServiceLoader serviceLoader;


    public ServiceConnectionSafeFromLoader(ServiceLoader serviceLoader, String serviceName) {
        super(serviceName);
        this.serviceLoader = serviceLoader;
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
            final IService<SC> service = (IService<SC>) serviceLoader.getService(serviceName);
            if (service == null) throw new ServiceNotFoundException(serviceName);
            serviceConnection = service.createServiceConnection();
            serviceConnection.ping();

        } finally {
            refreshLock.unlock();
        }
    }
}
