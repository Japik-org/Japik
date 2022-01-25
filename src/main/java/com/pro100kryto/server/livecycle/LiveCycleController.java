package com.pro100kryto.server.livecycle;

import com.pro100kryto.server.NotImplementedException;
import com.pro100kryto.server.logger.ILogger;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import static com.pro100kryto.server.livecycle.LiveCycleStatus.AdvancedNames.*;

public final class LiveCycleController implements ILiveCycle {
    private ILogger logger;
    private final String elementName;

    private final LiveCycleStatus status;
    private final ILiveCycleImpl defaultImpl;

    private ReentrantLock liveCycleLock;

    private IInit initRef;
    private IStart startRef;
    @Nullable
    private IStopSlow stopSlowRef;
    private IStopForce stopForceRef;
    private IDestroy destroyRef;
    @Nullable
    private ICanBeStoppedSafe canBeStoppedSafeRef;
    @Nullable
    private IAnnounceStop announceStopRef;

    private boolean isEnabledAutoFixBroken = false;

    @Setter
    @Accessors(chain = true)
    public static final class Builder {
        private ILiveCycleImpl defaultImpl;
        private ReentrantLock lock;

        private void initMissingValues(){
            if (defaultImpl == null){
                defaultImpl = EmptyLiveCycleImpl.instance;
            }
            if (lock == null){
                lock = new ReentrantLock();
            }
        }

        public LiveCycleController build(ILogger logger, String elementName){
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

        this.logger = Objects.requireNonNull(logger);
        this.elementName = elementName;
        this.liveCycleLock = Objects.requireNonNull(lock);
        this.status = new LiveCycleStatus(liveCycleLock);

        this.defaultImpl = Objects.requireNonNull(defaultImpl);

        setDefaultImpl();
    }

    public void setEnabledAutoFixBroken(boolean enabledAutoFixBroken) {
        this.isEnabledAutoFixBroken = enabledAutoFixBroken;
    }

    public synchronized void setLogger(@NotNull ILogger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    public synchronized void setInitImpl(@NotNull IInit action) {
        initRef = Objects.requireNonNull(action);
    }

    public synchronized void setStartImpl(@NotNull IStart start) {
        startRef = Objects.requireNonNull(start);
    }

    public synchronized void setStopSlowImpl(@Nullable IStopSlow stopSlow) {
        stopSlowRef = stopSlow;
    }

    public synchronized void setStopForceImpl(@NotNull IStopForce stopForce) {
        stopForceRef = Objects.requireNonNull(stopForce);
    }

    public synchronized void setDestroyImpl(@NotNull IDestroy destroy) {
        destroyRef = Objects.requireNonNull(destroy);
    }

    public synchronized void setCanBeStoppedSafeImpl(@Nullable ICanBeStoppedSafe canBeStoppedSafe) {
        canBeStoppedSafeRef = canBeStoppedSafe;
    }

    public synchronized void setAnnounceStopImpl(@Nullable IAnnounceStop announceStop) {
        announceStopRef = announceStop;
    }

    public synchronized void setAllImpl(@NotNull ILiveCycleImpl startStopAlive){
        Objects.requireNonNull(startStopAlive);

        setInitImpl(startStopAlive);
        setStartImpl(startStopAlive);
        setStopSlowImpl(startStopAlive);
        setStopForceImpl(startStopAlive);
        setDestroyImpl(startStopAlive);
        setCanBeStoppedSafeImpl(startStopAlive);
        setAnnounceStopImpl(startStopAlive);
    }

    public void setDefaultImpl(){
        setAllImpl(defaultImpl);
    }

    // ------------

    @Override
    public void init() throws InitException {
        StatusChecker.checkInit(status);

        final ReentrantLock liveCycleLock = this.liveCycleLock;
        liveCycleLock.lock();

        try {
            setStatus(INITIALIZING);

            try {
                initRef.init();
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
            status.setStopAnnounced(false);
            setStatus(STARTING);

            try {
                startRef.start();
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
    public boolean canBeStoppedSafe() {
        if (liveCycleLock.isLocked()) return false;
        if (!status.is(STARTED)) return false;
        if (canBeStoppedSafeRef == null) return true;

        try {
            return canBeStoppedSafeRef.canBeStoppedSafe();

        } catch (NotImplementedException notImplementedException){
            return true;

        } catch (Throwable throwable){
            logger.exception(throwable, "Failed canBeStoppedSafe for "+elementName);
            return false;
        }
    }

    @Override
    public void announceStop() {
        status.setStopAnnounced(true);

        if (announceStopRef != null) {
            try {
                announceStopRef.announceStop();

            } catch (NotImplementedException ignored) {

            } catch (Throwable throwable) {
                logger.exception(throwable, "Failed announceStop " + elementName);
            }
        }
    }

    @Override
    public void stopSlow() throws StopSlowException {
        StatusChecker.checkStopSlow(status);

        final ReentrantLock liveCycleLock = this.liveCycleLock;
        liveCycleLock.lock();

        try {
            setStatus(STOPPING_SLOW);

            if (!status.isStopAnnounced()) {
                announceStop();
            }

            try {
                stopSlowRef.stopSlow();
                setStatus(STOPPED);

            } catch (StopSlowException stopSlowException) {
                setStatus(stopSlowException.getNewStatus());
                throw stopSlowException;

            } catch (Throwable throwable) {
                setStatus(BROKEN);

                if (isEnabledAutoFixBroken) stopForce();

                if (stopSlowRef == null || throwable.getClass().equals(NotImplementedException.class)) {
                    throw new StopSlowException("Failed stopSlow (not supported) " + elementName, throwable, status.getAdvancedName());
                } else {
                    throw new StopSlowException("Failed stopSlow " + elementName, throwable, status.getAdvancedName());
                }
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
            stopForceRef.stopForce();
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
                destroyRef.destroy();

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
    public LiveCycleStatus getStatus() {
        return status;
    }

    @Override
    public ReentrantLock getLiveCycleLock() {
        return liveCycleLock;
    }

    private void setStatus(LiveCycleStatus.AdvancedNames statusName){
        this.status.set(statusName);
        logger.info(elementName+" status="+status);
    }
}
