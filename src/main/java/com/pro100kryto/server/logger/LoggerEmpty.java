package com.pro100kryto.server.logger;

public final class LoggerEmpty implements ILogger{
    public static final LoggerEmpty instance = new LoggerEmpty();

    @Override
    public String getName() {
        return "LoggerEmpty";
    }

    @Override
    public void exception(Throwable ex) {
    }

    @Override
    public void exception(Throwable ex, String description) {
    }

    @Override
    public void error(String msg) {
    }

    @Override
    public void warn(String msg) {
    }

    @Override
    public void info(String msg) {
    }

    @Override
    public void raw(String msg) {
    }
}
