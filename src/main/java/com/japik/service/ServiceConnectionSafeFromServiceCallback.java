package com.japik.service;

import java.rmi.RemoteException;

public final class ServiceConnectionSafeFromServiceCallback <SC extends IServiceConnection> extends AServiceConnectionSafe<SC> {
    private final IServiceCallback serviceCallback;


    public ServiceConnectionSafeFromServiceCallback(IServiceCallback serviceCallback, String serviceName) {
        super(serviceName);
        this.serviceCallback = serviceCallback;
    }

    /**
     * @throws ClassCastException - wrong service type
     */
    @Override
    public void refreshConnection() throws RemoteException {
        if (isClosed) throw new IllegalStateException();
        refreshLock.lock();
        try {
            final SC oldSC = serviceConnection;
            final SC newSC = serviceCallback.getServiceConnection(serviceName);
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
