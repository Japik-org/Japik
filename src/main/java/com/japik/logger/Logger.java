package com.japik.logger;

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
        final StringBuilder stringBuilder = new StringBuilder();
        appendExceptionWithCause(stringBuilder, ex);

        loggerManager.write(name, MsgType.EXCEPTION, stringBuilder.toString());
        ex.printStackTrace();
    }

    @Override
    public void exception(Throwable ex, String description) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DESCRIPTION: ")
                .append(description)
                .append(System.lineSeparator());
        appendExceptionWithCause(stringBuilder, ex);

        loggerManager.write(name, MsgType.EXCEPTION, stringBuilder.toString());
        ex.printStackTrace();
    }

    private void appendSingleException(StringBuilder stringBuilder, Throwable ex) {
        stringBuilder.append(ex.getClass().getSimpleName())
                .append(" : ")
                .append(ex.getMessage())
                .append(System.lineSeparator());
    }

    private void appendExceptionWithCause(StringBuilder stringBuilder, Throwable ex) {
        appendSingleException(stringBuilder, ex);

        Throwable cause = ex;
        int count = 0;
        while ((cause = cause.getCause()) != null) {
            stringBuilder.append("CAUSE: ");
            appendSingleException(stringBuilder, cause);
            if (++count == 32) {
                stringBuilder.append("(cause overflow)");
                break;
            }
        }
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
    public void warn(String msg, Throwable cause) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(msg)
                .append(System.lineSeparator())
                .append("WARN CAUSE: ");
        appendExceptionWithCause(stringBuilder, cause);
        warn(stringBuilder.toString());
        cause.printStackTrace();
    }

    @Override
    public void warn(Throwable cause) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("WARN CAUSE: ");
        appendExceptionWithCause(stringBuilder, cause);
        warn(stringBuilder.toString());
        cause.printStackTrace();
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
