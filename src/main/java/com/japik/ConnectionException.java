package com.japik;

import java.rmi.RemoteException;

public abstract class ConnectionException extends RemoteException {
    public ConnectionException() {
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
