package com.japik.service;

import lombok.SneakyThrows;

import java.rmi.RemoteException;

public final class ServiceConnectionSafeFromLoader <SC extends IServiceConnection> extends AServiceConnectionSafe<SC> {
    private final ServiceLoader serviceLoader;


    public ServiceConnectionSafeFromLoader(ServiceLoader serviceLoader, String serviceName) {
        super(serviceName);
        this.serviceLoader = serviceLoader;
    }


    @SneakyThrows
    @Override
    public SC getServiceConnection() throws RemoteException {
        return super.getServiceConnection();
    }

    /**
     * @throws ClassCastException - wrong service type
     */
    @Override
    public void refreshConnection() throws RemoteException {
        if (isClosed) throw new IllegalStateException();
        refreshLock.lock();
        try {
            // !! ClassCastException !!
            final IService<SC> service = (IService<SC>) serviceLoader.getOrThrow(serviceName);

            final SC oldSC = serviceConnection;
            final SC newSC = service.getServiceConnection();
            if (oldSC != newSC && oldSC != null && !oldSC.isClosed()) {
                oldSC.close();
            }
            serviceConnection = newSC;

            try {
                serviceConnection.ping();
            } catch (Throwable throwable) {
                isClosed = true;
                if (serviceConnection != null) {
                    try {
                        serviceConnection.close();
                    } catch (Throwable ignored){
                    }
                }
                serviceConnection = null;
                throw throwable;
            }
            isClosed = false;

        } catch (RemoteException remoteException) {
            throw remoteException;

        } catch (Throwable throwable){
            throw new ServiceConnectionException(
                    serviceName,
                    throwable
            );

        } finally {
            refreshLock.unlock();
        }
    }
}
