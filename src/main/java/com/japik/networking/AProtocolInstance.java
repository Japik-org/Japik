package com.japik.networking;

import com.japik.service.IServiceConnection;
import com.japik.service.IServiceConnectionSafe;
import lombok.Getter;

import java.rmi.RemoteException;

public abstract class AProtocolInstance implements IProtocolInstance {
    @Getter
    private final String protocolName;
    @Getter
    private boolean isClosed = false;

    public AProtocolInstance(String protocolName) {
        this.protocolName = protocolName;
    }

    @Override
    public synchronized final boolean existsService(String serviceName) throws RemoteException {
        throwIfClosed();
        return existsServiceImpl(serviceName);
    }
    protected abstract boolean existsServiceImpl(String serviceName) throws RemoteException;

    @Override
    public synchronized final IServiceConnection getServiceConnection(String serviceName) throws RemoteException {
        throwIfClosed();
        return getServiceConnectionImpl(serviceName);
    }
    protected abstract IServiceConnection getServiceConnectionImpl(String serviceName) throws RemoteException;

    @Override
    public synchronized final <SC extends IServiceConnection> IServiceConnectionSafe<SC> createServiceConnectionSafe(String serviceName) throws RemoteException {
        throwIfClosed();
        return createServiceConnectionSafeImpl(serviceName);
    }
    protected abstract <SC extends IServiceConnection> IServiceConnectionSafe<SC> createServiceConnectionSafeImpl(String serviceName) throws RemoteException;

    @Override
    public synchronized final void close() throws Exception {
        isClosed = true;
        onClose();
    }

    protected abstract void onClose() throws Exception;

    protected final void throwIfClosed() {
        if (isClosed) throw new IllegalStateException("Protocol instance '"+protocolName+"' is closed.");
    }
}
