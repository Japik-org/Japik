package com.pro100kryto.server.service;

import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.logger.LoggerAlreadyExistsException;

public interface IServiceCallback {
    // logger
    ILogger createLogger(String loggerSubName) throws LoggerAlreadyExistsException;
    ILogger getLogger(String loggerSubName);
    boolean existsLogger(String loggerSubName);

    // connection

    /**
     * @throws ClassCastException
     */
    <SC extends IServiceConnection> SC getServiceConnection(String serviceName) throws ServiceNotFoundException;
    <SC extends IServiceConnection> IServiceConnectionSafe<SC> createServiceConnectionSafe(String serviceName);
}
