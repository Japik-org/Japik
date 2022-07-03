package com.japik.service;

import com.japik.element.ElementNotFoundException;
import com.japik.logger.ILogger;
import com.japik.logger.LoggerAlreadyExistsException;

public interface IServiceCallback {
    // logger
    ILogger createLogger(String loggerSubName) throws LoggerAlreadyExistsException;
    ILogger getLogger(String loggerSubName);
    boolean existsLogger(String loggerSubName);

    // connection

    /**
     * @throws ClassCastException
     */
    <SC extends IServiceConnection> SC getServiceConnection(String serviceName) throws ElementNotFoundException;
    <SC extends IServiceConnection> IServiceConnectionSafe<SC> createServiceConnectionSafe(String serviceName);
}
