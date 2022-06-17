package com.pro100kryto.server.livecycle.controller;

import com.pro100kryto.server.NotImplementedException;
import com.pro100kryto.server.livecycle.*;
import com.pro100kryto.server.logger.EmptyLogger;
import com.pro100kryto.server.logger.ILogger;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static com.pro100kryto.server.livecycle.LiveCycleStatus.AdvancedNames.*;

@Getter
public final class LiveCycleController implements ILiveCycle {
    private ILogger logger;
    private final String elementName;
    private final LiveCycleStatus status;
    private ReentrantLock liveCycleLock;
    private boolean isEnabledAutoFixBroken = false;

    //region layer
    private final ILiveCycleImpl defaultImpl;
    private final ILiveCycleImplId defaultInitImplId = new LiveCycleImplId(
            "defaultInit",
            PRIORITY_NORMAL
    );

    private final ImplQueue<IInit> initImplQueue = new ImplQueue<>();
    private final ImplQueue<IStart> startImplQueue = new ImplQueue<>();
    private final ImplQueue<IStopSlow> stopSlowImplQueue = new ImplQueue<>();
    private final ImplQueue<IStopForce> stopForceImplQueue = new ImplQueue<>();
    private final ImplQueue<IDestroy> destroyImplQueue = new ImplQueue<>();

    private final ImplMap<ICanBeStoppedSafe> canBeStoppedSafeImplMap = new ImplMap<>();
    private final ImplMap<IAnnounceStop> announceStopImplMap = new ImplMap<>();

    public static final int PRIORITY_HIGHEST = Integer.MIN_VALUE;
    public static final int PRIORITY_HIGH = -1;
    public static final int PRIORITY_NORMAL = 0;
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_LOWER = Integer.MAX_VALUE;
    //endregion

    @Setter
    @Accessors(chain = true)
    public static final class Builder {
        private ILiveCycleImpl defaultImpl;
        private ReentrantLock lock;
        private String elementName;
        private ILogger logger;

        private void initMissingValues(){
            if (defaultImpl == null){
                defaultImpl = EmptyLiveCycleImpl.instance;
            }
            if (lock == null){
                lock = new ReentrantLock();
            }
            if (elementName == null) {
                elementName = "No element name";
            }
            if (logger == null) {
                logger = EmptyLogger.instance;
            }
        }

        public LiveCycleController build(){
            initMissingValues();
            return new LiveCycleController(
                    logger,
                    elementName,
                    defaultImpl,
                    lock
            );
        }
    }

    /**
     * @throws NullPointerException
     */
    public LiveCycleController(ILogger logger, String elementName,
                               ILiveCycleImpl defaultImpl,
                               ReentrantLock lock) {
        this.defaultImpl = defaultImpl;

        this.logger = Objects.requireNonNull(logger);
        this.elementName = elementName;
        this.liveCycleLock = Objects.requireNonNull(lock);
        this.status = new LiveCycleStatus(liveCycleLock);

        restoreDefaultImpl();
    }

    public void setEnabledAutoFixBroken(boolean enabledAutoFixBroken) {
        this.isEnabledAutoFixBroken = enabledAutoFixBroken;
    }

    public void setLogger(@NotNull ILogger logger) {
        liveCycleLock.lock();
        try {
            this.logger = Objects.requireNonNull(logger);
        } finally {
            liveCycleLock.unlock();
        }
    }

    // ------------

    //region layer

    public <T extends ILiveCycleImplId & ILiveCycleImpl> void putImplQueue(T idAndImpl) {
        putImplQueue(idAndImpl, idAndImpl);
    }

    public void putImplQueue(ILiveCycleImplId id, @NotNull ILiveCycleImpl liveCycleImpl){
        Objects.requireNonNull(liveCycleImpl);

        initImplQueue.put(id, liveCycleImpl);
        startImplQueue.put(id, liveCycleImpl);
        stopSlowImplQueue.put(id, liveCycleImpl);
        stopForceImplQueue.put(id, liveCycleImpl);
        destroyImplQueue.put(id, liveCycleImpl);
    }

    public <T extends ILiveCycleImplId & ILiveCycleImpl> void putImplAll(T idAndImpl) {
        putImplAll(idAndImpl, idAndImpl);
    }

    public void putImplAll(ILiveCycleImplId id, @NotNull ILiveCycleImpl liveCycleImpl){
        Objects.requireNonNull(liveCycleImpl);

        initImplQueue.put(id, liveCycleImpl);
        startImplQueue.put(id, liveCycleImpl);
        stopSlowImplQueue.put(id, liveCycleImpl);
        stopForceImplQueue.put(id, liveCycleImpl);
        destroyImplQueue.put(id, liveCycleImpl);
        canBeStoppedSafeImplMap.put(id, liveCycleImpl);
        announceStopImplMap.put(id, liveCycleImpl);
    }

    public void clearImplQueue() {
        initImplQueue.clear();
        startImplQueue.clear();
        stopSlowImplQueue.clear();
        stopForceImplQueue.clear();
        destroyImplQueue.clear();
    }

    public void clearImplAll() {
        initImplQueue.clear();
        startImplQueue.clear();
        stopSlowImplQueue.clear();
        stopForceImplQueue.clear();
        destroyImplQueue.clear();
        canBeStoppedSafeImplMap.clear();
        announceStopImplMap.clear();
    }

    public void restoreDefaultImpl(){
        clearImplAll();
        putImplAll(defaultInitImplId, defaultImpl);
    }

    //endregion

    @Override
    public void init() throws InitException {
        StatusChecker.checkInit(status);

        final ReentrantLock liveCycleLock = this.liveCycleLock;
        liveCycleLock.lock();

        try {
            try {
                setStatus(INITIALIZING);

                while (!initImplQueue.isEmpty()) {
                    final Pair<ILiveCycleImplId, IInit> pair = initImplQueue.peek();
                    pair.getTwo().init();
                    initImplQueue.remove(pair);
                }
                setStatus(INITIALIZED);

            } catch (InitException initException) {
                setStatus(initException.getNewStatus());

                throw initException;

            } catch (Throwable throwable) {
                setStatus(BROKEN);

                if (isEnabledAutoFixBroken) {
                    destroy();
                }

                throw new InitException("Failed init " + elementName, throwable, status.getAdvancedName());
            }

        } finally {
            liveCycleLock.unlock();
        }
    }

    @Override
    public void start() throws StartException {
        StatusChecker.checkStart(status);

        final ReentrantLock liveCycleLock = this.liveCycleLock;
        liveCycleLock.lock();

        try {
            try {
                status.setStopAnnounced(false);
                setStatus(STARTING);

                while (!startImplQueue.isEmpty()) {
                    final Pair<ILiveCycleImplId, IStart> pair = startImplQueue.peek();
                    pair.getTwo().start();
                    startImplQueue.remove(pair);
                }
                setStatus(STARTED);

            } catch (StartException startException) {
                setStatus(startException.getNewStatus());
                throw startException;

            } catch (Throwable throwable) {
                setStatus(BROKEN);

                if (isEnabledAutoFixBroken) {
                    stopForce();
                }

                throw new StartException("Failed start " + elementName, throwable, status.getAdvancedName());
            }

        } finally {
            liveCycleLock.unlock();
        }
    }

    @Override
    public void stopSlow() throws StopSlowException {
        StatusChecker.checkStopSlow(status);

        final ReentrantLock liveCycleLock = this.liveCycleLock;
        liveCycleLock.lock();

        try {
            try {
                setStatus(STOPPING_SLOW);

                if (!status.isStopAnnounced()) {
                    announceStop();
                }

                while (!stopSlowImplQueue.isEmpty()) {
                    final Pair<ILiveCycleImplId, IStopSlow> pair = stopSlowImplQueue.peek();
                    pair.getTwo().stopSlow();
                    stopSlowImplQueue.remove(pair);
                }
                setStatus(STOPPED);

            } catch (StopSlowException stopSlowException) {
                setStatus(stopSlowException.getNewStatus());
                throw stopSlowException;

            } catch (NotImplementedException notImplementedException) {
                throw new StopSlowException("Failed stopSlow (not implemented) " + elementName, notImplementedException, status.getAdvancedName());

            } catch (Throwable throwable) {
                setStatus(BROKEN);

                if (isEnabledAutoFixBroken) stopForce();

                throw new StopSlowException("Failed stopSlow " + elementName, throwable, status.getAdvancedName());
            }

        } finally {
            liveCycleLock.unlock();
        }
    }

    @Override
    public void stopForce() {
        StatusChecker.checkStopForce(status);
        setStatus(STOPPING_FORCE);

        if (liveCycleLock.isLocked()) {
            logger.warn("liveCycleLock holdCount = " + liveCycleLock.getHoldCount());
        }

        if (!status.isStopAnnounced()) {
            try {
                announceStop();
            } catch (Throwable throwable){
                logger.exception(throwable, "Failed announceStop "+elementName);
            }
        }

        try{
            while (!stopForceImplQueue.isEmpty()) {
                final Pair<ILiveCycleImplId, IStopForce> pair = stopForceImplQueue.peek();
                pair.getTwo().stopForce();
                stopForceImplQueue.remove(pair);
            }
            setStatus(STOPPED);

        } catch (Throwable throwable){
            setStatus(BROKEN);
            logger.exception(throwable, "Failed stopForce "+elementName);

            if (isEnabledAutoFixBroken) destroy();
        }
    }

    @Override
    public void destroy() {
        StatusChecker.checkDestroy(status);

        final ReentrantLock liveCycleLock = this.liveCycleLock;
        final boolean isLockedSucc = liveCycleLock.tryLock();

        try {
            try {
                setStatus(DESTROYED);

                while (!destroyImplQueue.isEmpty()) {
                    final Pair<ILiveCycleImplId, IDestroy> pair = destroyImplQueue.peek();
                    pair.getTwo().destroy();
                    destroyImplQueue.remove(pair);
                }

                restoreDefaultImpl();

            } catch (Throwable throwable) {
                setStatus(BROKEN);
                logger.exception(throwable, "Failed destroy " + elementName);
            }

            if (!isLockedSucc && liveCycleLock.isLocked()) {
                this.liveCycleLock = new ReentrantLock();
                logger.warn("liveCycleLock realized");
            }

        } finally {
            if (isLockedSucc) liveCycleLock.unlock();
        }
    }

    @Override
    public boolean canBeStoppedSafe() {
        if (liveCycleLock.isLocked()) return false;
        if (!status.is(STARTED)) return false;

        for (final Pair<ILiveCycleImplId, ICanBeStoppedSafe> pair : canBeStoppedSafeImplMap.values()) {
            try{
                if (!pair.getTwo().canBeStoppedSafe()) return false;

            } catch (NotImplementedLiveCycleOperation ignored){

            } catch (Throwable throwable) {
                logger.exception(throwable, "Failed canBeStoppedSafe elementName='"+elementName+
                        "' idName='"+pair.getOne().getName()+"'. Return false.");
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void announceStop() {
        status.setStopAnnounced(true);

        for (final Pair<ILiveCycleImplId, IAnnounceStop> pair : announceStopImplMap.values()) {
            try{
                pair.getTwo().announceStop();

            } catch (NotImplementedLiveCycleOperation ignored){

            } catch (Throwable throwable) {
                logger.exception(throwable, "Failed announceStop elementName='"+elementName+
                        "' idName='"+pair.getOne().getName()+"'");
            }
        }
    }

    private void setStatus(LiveCycleStatus.AdvancedNames statusName){
        this.status.set(statusName);
        logger.info(elementName+" status = "+status);
    }
}
