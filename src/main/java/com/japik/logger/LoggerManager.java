package com.japik.logger;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class LoggerManager {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, ILoggerListener> loggerListenerMap = new HashMap<>();
    private final Map<String, ILogger> loggerMap = Collections.synchronizedMap(new HashMap<>());
    private final List<ILoggerChangesListener> loggerLChangesListeners = new ArrayList<>();
    @Getter
    private final ILogger mainLogger;
    @NotNull
    private ILoggerListener mainLoggerListener;

    public LoggerManager() {
        mainLoggerListener = new LoggerListenerConsole();
        setLoggerListener("main", mainLoggerListener);

        mainLogger = new Logger(this, "main");
        try {
            registerLogger(mainLogger);
        } catch (LoggerAlreadyExistsException ignored){
        }
        Loggers.setDefaultLogger(mainLogger);
    }

    // listener

    public void setMainLoggerListener(@NotNull ILoggerListener loggerListener){
        Objects.requireNonNull(loggerListener);

        lock.lock();
        try {

            mainLogger.info("Setting new mainLoggerListener");
            mainLoggerListener = loggerListener;
            setLoggerListener("main", mainLoggerListener);
            try {
                mainLoggerListener.write("main", LocalDateTime.now(), MsgType.INFO, "test message");
                mainLogger.info("New mainLoggerListener connected");
            } catch (Throwable throwable) {
                resetMainLoggerListener();
                mainLogger.exception(throwable, "Failed set mainLoggerListener");
            }
        } finally {
            lock.unlock();
        }
    }

    public void resetMainLoggerListener(){
        lock.lock();
        try {
            setMainLoggerListener(new LoggerListenerConsole());
        } finally {
            lock.unlock();
        }
    }

    public void setLoggerListener(String loggerName, @Nullable ILoggerListener loggerListener){
        lock.lock();
        try {

            write(loggerName, MsgType.INFO, "Setting new loggerListener for '" + loggerName + "'");

            loggerListenerMap.put(loggerName, loggerListener);
            try {
                write(loggerName, MsgType.INFO, "This LoggerListener '" + loggerName + "' was connected");
            } catch (NullPointerException ignored) {
            }

        } finally {
            lock.unlock();
        }
    }

    public void removeLoggerListener(String loggerName){
        lock.lock();
        try {
            if (!loggerListenerMap.containsKey(loggerName)) return;
            write(loggerName, MsgType.INFO, "Disconnecting loggerListener '" + loggerName + "'");
            loggerListenerMap.remove(loggerName);

        } finally {
            lock.unlock();
        }
    }

    public void removeLoggerListeners(){
        lock.lock();
        try {

            for (String loggerName : getLoggerListenerNames()) {
                removeLoggerListener(loggerName);
            }

        } finally {
            lock.unlock();
        }
    }

    public void addLoggerChangesListener(ILoggerChangesListener loggerChangesListener){
        lock.lock();
        try {

            loggerLChangesListeners.add(loggerChangesListener);
            loggerChangesListener.onLoggerRegistered("main");

        } finally {
            lock.unlock();
        }
    }

    public void removeLoggerChangesListener(ILoggerChangesListener loggerChangesListener){
        lock.lock();
        try {
            loggerLChangesListeners.remove(loggerChangesListener);
        } finally {
            lock.unlock();
        }
    }

    public Iterable<String> getLoggerListenerNames(){
        return loggerListenerMap.keySet();
    }

    // logger

    public boolean existsLogger(String loggerName) {
        lock.lock();
        try {
            return loggerMap.containsKey(loggerName);
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public ILogger getLogger(String loggerName){
        lock.lock();
        try {
            return loggerMap.get(loggerName);
        } finally {
            lock.unlock();
        }
    }

    public ILogger createLogger(String loggerName) throws LoggerAlreadyExistsException {
        lock.lock();
        try {

            {
                final ILogger logger = loggerMap.get(loggerName);
                if (logger != null) {
                    throw new LoggerAlreadyExistsException(logger);
                }
            }

            final ILogger logger = new Logger(this, loggerName);
            registerLogger(logger);
            return logger;

        } finally {
            lock.unlock();
        }
    }

    public ILogger getOrCreateLogger(String loggerName) {
        lock.lock();
        try {
            final ILogger logger = getLogger(loggerName);
            if (logger == null) {
                try {
                    return createLogger(loggerName);
                } catch (LoggerAlreadyExistsException loggerAlreadyExistsException) {
                    throw new RuntimeException(loggerAlreadyExistsException);
                }
            }
            return logger;
        } finally {
            lock.unlock();
        }
    }

    public void registerLogger(@NotNull ILogger logger) throws LoggerAlreadyExistsException {
        lock.lock();
        try {

            if (loggerMap.containsKey(logger.getName())) {
                throw new LoggerAlreadyExistsException(loggerMap.get(logger.getName()));
            }

            write(logger.getName(), MsgType.INFO, "Connecting new logger '" + logger.getName() + "'");
            loggerMap.put(logger.getName(), logger);

            for (ILoggerChangesListener listener : loggerLChangesListeners) {
                listener.onLoggerRegistered(logger.getName());
            }

            logger.info("Logger '" + logger.getName() + "' connected");

        } finally {
            lock.unlock();
        }
    }

    public void unregisterLogger(String loggerName){
        lock.lock();
        try {
            write(loggerName, MsgType.INFO, "Disconnecting the logger '" + loggerName + "'");
            loggerMap.remove(loggerName);

            for (final ILoggerChangesListener listener : loggerLChangesListeners) {
                listener.onLoggerUnregistered(loggerName);
            }
        } finally {
            lock.unlock();
        }
    }

    public void unregisterLoggers(){
        lock.lock();
        try {
            for (final String loggerName : getLoggerNames()) {
                unregisterLogger(loggerName);
            }
        } finally {
            lock.unlock();
        }
    }

    public Iterable<String> getLoggerNames(){
        return loggerMap.keySet();
    }

    @Deprecated
    public void write(String loggerName, MsgType msgType, String msg) {
        try {
            loggerListenerMap.get(loggerName).write(loggerName, LocalDateTime.now(), msgType, msg);
            return;
        } catch (Throwable ignored){
        }
        
        mainLoggerListener.write(loggerName, LocalDateTime.now(), msgType, msg);
    }
}
