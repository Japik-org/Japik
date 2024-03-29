package com.japik.logger;

public interface ILogger {
    String getName();

    void exception(Throwable ex);
    void exception(Throwable ex, String description);
    void error(String msg);
    void warn(String msg);
    void warn(String msg, Throwable cause);
    void warn(Throwable cause);
    void info(String msg);
    void raw(String msg);
}
