package com.pro100kryto.server.logger;

public class Logger implements ILogger {
    protected final LoggerManager loggerManager;
    protected final String name;

    public Logger(LoggerManager loggerManager, String name) {
        this.loggerManager = loggerManager;
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public void exception(Throwable ex) {
        ex.printStackTrace();
        loggerManager.write(name, MsgType.EXCEPTION, ex.getClass().getSimpleName()+" : "+ex.getMessage()
                +(ex.getCause()!=null ? " CAUSE: "+ex.getCause().getMessage() : ""));
    }

    @Override
    public void exception(Throwable ex, String description) {
        ex.printStackTrace();
        loggerManager.write(name, MsgType.EXCEPTION, ex.getClass().getSimpleName()+" : "+ex.getMessage()
                +(ex.getCause()!=null ? " CAUSE: "+ex.getCause().getMessage() : "")
                +" DESCRIPTION: "+description);
    }

    @Override
    public void error(String msg) {
        loggerManager.write(name, MsgType.ERROR, msg);
    }

    @Override
    public void warn(String msg) {
        loggerManager.write(name, MsgType.WARN, msg);
    }

    @Override
    public void info(String msg) {
        loggerManager.write(name, MsgType.INFO, msg);
    }

    @Override
    public void raw(String msg) {
        loggerManager.write(name, MsgType.RAW, msg);
    }
}
