package com.japik.logger;

public interface ILoggerChangesListener {
    void onLoggerRegistered(String loggerName);
    void onLoggerUnregistered(String loggerName);
}
