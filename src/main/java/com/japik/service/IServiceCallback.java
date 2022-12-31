package com.japik.service;

import com.japik.element.ElementNotFoundException;
import com.japik.logger.ILogger;
import com.japik.logger.LoggerAlreadyExistsException;

import java.rmi.RemoteException;

public interface IServiceCallback {
    // logger
    ILogger createLogger(String loggerSubName) throws LoggerAlreadyExistsException;
    ILogger getLogger(String loggerSubName);
    boolean existsLogger(String loggerSubName);

    // connection

    /**
     * @throws ClassCastException
     */
    <SC extends IServiceConnection> SC getServiceConnection(String serviceName) throws RemoteException, ServiceNotFoundException;
    <SC extends IServiceConnection> IServiceConnectionSafe<SC> createServiceConnectionSafe(String serviceName);
}
