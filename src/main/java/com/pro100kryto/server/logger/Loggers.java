package com.pro100kryto.server.logger;

import java.util.Objects;

public final class Loggers {
    private static ILogger defaultLogger = null;

    public static ILogger getDefaultLogger(){
        checkLogger();
        return defaultLogger;
    }

    public static void setDefaultLogger(ILogger logger){
        defaultLogger = Objects.requireNonNull(logger);
    }

    private static void checkLogger(){
        if (defaultLogger == null){
            defaultLogger = new LoggerSystemOut();
        }
    }
}
