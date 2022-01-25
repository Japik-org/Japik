package com.pro100kryto.server.logger;

public final class LoggerSystemOut implements ILogger{
    public static final LoggerSystemOut instance = new LoggerSystemOut();

    @Override
    public String getName() {
        return "LoggerStacktrace";
    }

    @Override
    public void exception(Throwable ex) {
        ex.printStackTrace();
    }

    @Override
    public void exception(Throwable ex, String description) {
        error(description);
        ex.printStackTrace();
    }

    @Override
    public void error(String msg) {
        raw("(error) "+msg);
    }

    @Override
    public void warn(String msg) {
        raw("(warn) "+msg);
    }

    @Override
    public void warn(String msg, Throwable cause) {
        warn(msg);
        cause.printStackTrace();
    }

    @Override
    public void info(String msg) {
        raw("(info) "+msg);
    }

    @Override
    public void raw(String msg) {
        System.out.println(msg);
    }
}
