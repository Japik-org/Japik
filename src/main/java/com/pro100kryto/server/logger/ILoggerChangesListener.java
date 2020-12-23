package com.pro100kryto.server.logger;

public interface ILoggerChangesListener {
    void onLoggerRegistered(String loggerName);
    void onLoggerUnregistered(String loggerName);
}
