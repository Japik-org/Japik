package com.pro100kryto.server.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.time.LocalDateTime;
import java.util.*;

public final class LoggerManager {
    @NotNull
    private ILoggerListener mainLoggerListener;
    private final Map<String, ILoggerListener> loggerListenerMap;
    private final Map<String, ILogger> loggerMap;
    private final List<ILoggerChangesListener> loggerLChangesListeners;
    private final ILogger mainLogger;


    public LoggerManager() {
        mainLoggerListener = new LoggerListenerConsole();
        loggerListenerMap = new HashMap<>();
        loggerMap = new HashMap<>();
        loggerLChangesListeners = new ArrayList<>();

        mainLogger = new Logger(this, "main");
        setLoggerListener("main", mainLoggerListener);

        registerLogger(mainLogger);
    }

    public void setMainLoggerListener(@NotNull ILoggerListener loggerListener){
        Objects.requireNonNull(loggerListener);
        mainLogger.writeInfo("Setting new mainLoggerListener");
        mainLoggerListener = loggerListener;
        setLoggerListener("main", mainLoggerListener);
        try {
            mainLoggerListener.write("main", LocalDateTime.now(), MsgType.INFO, "test message");
            mainLogger.writeInfo("New mainLoggerListener connected");
        } catch (Throwable throwable){
            resetMainLoggerListener();
            mainLogger.writeException(throwable, "Failed set mainLoggerListener");
        }
    }

    public void resetMainLoggerListener(){
        setMainLoggerListener(new LoggerListenerConsole());
    }


    public void setLoggerListener(String loggerName, @Nullable ILoggerListener loggerListener){
        write(loggerName, MsgType.INFO, "Setting new loggerListener for '"+loggerName+"'");

        loggerListenerMap.put(loggerName, loggerListener);
        try {
            write(loggerName, MsgType.INFO, "This LoggerListener '"+loggerName+"' was connected");
        } catch (NullPointerException ignored){
        }
    }

    public void removeLoggerListener(String loggerName){
        if (!loggerListenerMap.containsKey(loggerName)) return;
        write(loggerName, MsgType.INFO, "Disconnecting loggerListener '"+loggerName+"'");
        loggerListenerMap.remove(loggerName);
    }

    public void removeLoggerListenerAll(){
        for (String loggerName : getLoggerListenerNames()){
            removeLoggerListener(loggerName);
        }
    }

    public synchronized void addLoggerChangesListener(ILoggerChangesListener loggerChangesListener){
        loggerLChangesListeners.add(loggerChangesListener);
        loggerChangesListener.onLoggerRegistered("main");
    }

    public synchronized void removeLoggerChangesListener(ILoggerChangesListener loggerChangesListener){
        loggerLChangesListeners.remove(loggerChangesListener);
    }

    public ILogger getMainLogger() {
        return mainLogger;
    }

    @Nullable
    public ILogger getLogger(String loggerName){
        return loggerMap.get(loggerName);
    }

    public ILogger createLogger(String loggerName) throws KeyAlreadyExistsException {
        if (loggerMap.containsKey(loggerName))
            throw new KeyAlreadyExistsException("Logger with name '"+loggerName+"' already exists");
        ILogger logger = new Logger(this, loggerName);
        registerLogger(logger);
        return logger;
    }

    public synchronized void registerLogger(@NotNull ILogger logger){
        write(logger.getName(), MsgType.INFO, "Connecting new logger '"+logger.getName()+"'");
        loggerMap.put(logger.getName(), logger);

        for (ILoggerChangesListener listener : loggerLChangesListeners) {
            listener.onLoggerRegistered(logger.getName());
        }

        logger.writeInfo("Logger '"+logger.getName()+"' connected");
    }

    public synchronized void unregisterLogger(String loggerName){
        write(loggerName, MsgType.INFO, "Disconnecting the logger '"+loggerName+"'");
        loggerMap.remove(loggerName);

        for (ILoggerChangesListener listener : loggerLChangesListeners) {
            listener.onLoggerUnregistered(loggerName);
        }
    }

    public void unregisterLoggerAll(){
        for (String loggerName : getLoggerNames()){
            unregisterLogger(loggerName);
        }
    }

    
    public String[] getLoggerNames(){
        return (String[]) loggerMap.keySet().toArray();
    }

    public String[] getLoggerListenerNames(){
        return (String[]) loggerListenerMap.keySet().toArray();
    }


    public void write(String loggerName, MsgType msgType, String msg) {
        try {
            loggerListenerMap.get(loggerName).write(loggerName, LocalDateTime.now(), msgType, msg);
            return;
        } catch (Throwable ignored){
        }
        
        mainLoggerListener.write(loggerName, LocalDateTime.now(), msgType, msg);
    }
}
