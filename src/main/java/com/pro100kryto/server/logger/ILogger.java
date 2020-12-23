package com.pro100kryto.server.logger;

public interface ILogger {
    String getName();

    void writeException(Throwable ex);
    void writeException(Throwable ex, String description);
    void writeError(String msg);
    void writeWarn(String msg);
    void writeInfo(String msg);
    void write(String msg);
}
