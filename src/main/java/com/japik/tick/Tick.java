package com.japik.tick;

import com.japik.logger.ILogger;
import lombok.Getter;
import lombok.Setter;

public final class Tick implements ITickRunnableCallback, ITick, ITickSettings {
    @Getter
    private final long id;
    private final ITickCallback tickGroup;
    private final ILogger logger;
    private final ATickRunnable tickRunnable;
    private long dtCounter;
    @Getter
    private TickStatus status = TickStatus.DESTROYED;

    @Getter @Setter
    private long maxTicksCount = -1;
    private final Object ticksCountLocker = new Object();
    @Getter
    private long ticksCount = 0;
    private int ticksCounterPerSec;
    @Getter
    private float ticksPerSec = 0;
    private long lastTickTime;

    @Getter @Setter
    private long delay = 0;

    public Tick(long id, ITickCallback callback, ILogger logger, ATickRunnable tickRunnable) {
        this.id = id;
        this.tickGroup = callback;
        this.logger = logger;
        this.tickRunnable = tickRunnable;
        this.tickRunnable.setTickRunnableCallback(this);
    }

    public void tick(){
        // calculate time
        final long currentTime = System.currentTimeMillis();
        final long timeDiff = currentTime-lastTickTime;
        lastTickTime = currentTime;

        // calc ticks per sec
        ticksCounterPerSec++;

        synchronized (ticksCountLocker) {
            if (getStatus() != TickStatus.ACTIVATED) return;
            ticksCount = Math.max(ticksCount + 1, 0);
            if (ticksCount == maxTicksCount) {
                inactivate();
                return;
            }
        }

        if ((dtCounter+=timeDiff)>1000) {
            ticksPerSec = (float) ticksCounterPerSec / dtCounter;
            dtCounter -= 1000;
            ticksCounterPerSec = 0;
        }

        try {
            tickRunnable.tick(timeDiff);
        } catch (Throwable throwable){
            logger.exception(throwable, "Failed "+toString());
        }
    }

    @Override
    public synchronized void init(){
        if (status != TickStatus.DESTROYED)
            throw new IllegalStateException("Failed init tick #"+id+" because is already initialized");

        setStatus(TickStatus.INITIALIZING);
        tickRunnable.beforeInit();
        tickRunnable.init();
        tickRunnable.afterInit();
        setStatus(TickStatus.INITIALIZED);
    }

    @Override
    public synchronized void activate(){
        if (status == TickStatus.ACTIVATED) throw new IllegalStateException("Failed activate tick #"+id+" because is already activated");

        setStatus(TickStatus.ACTIVATING);
        try {
            tickRunnable.beforeActivate();
        } catch (Throwable throwable){
            logger.exception(throwable, "Error occurred while activating tick #"+id+" (beforeActivate)");
        }

        lastTickTime = System.currentTimeMillis();
        tickGroup.setTickActive(id);

        setStatus(TickStatus.ACTIVATED);
        try {
            tickRunnable.afterActivate();
        } catch (Throwable throwable){
            logger.exception(throwable, "Error occurred while activating tick #"+id+" (afterActivate)");
        }
    }

    @Override
    public synchronized void inactivate(){
        if (status == TickStatus.INACTIVATED) throw new IllegalStateException("Failed pause tick #"+id+" because is already paused");
        if (status != TickStatus.ACTIVATED) throw new IllegalStateException("Failed pause tick #"+id+" because is not started");

        setStatus(TickStatus.INACTIVATING);
        try {
            tickRunnable.beforeInactivate();
        } catch (Throwable throwable){
            logger.exception(throwable, "Error occurred while pausing tick #"+id+" (beforeInactivate)");
        }

        tickGroup.setTickInactive(id);

        setStatus(TickStatus.INACTIVATED);
        try {
            tickRunnable.afterInactivate();
        } catch (Throwable throwable){
            logger.exception(throwable, "Error occurred while pausing tick #"+id+" (afterInactivate)");
        }
    }

    @Override
    public ITickSettings getTickSettings() {
        return this;
    }

    @Override
    public synchronized void destroy(){
        if (status == TickStatus.DESTROYED)
            throw new IllegalStateException("Failed destroy tick #"+id+" because is already destroyed");
        //if (status == TickStatus.ACTIVATED) throw new IllegalStateException("Failed destroy tick #"+id+" because is ACTIVATED");

        if (status == TickStatus.ACTIVATED){
            inactivate();
        }

        setStatus(TickStatus.DESTROYING);
        try {

            tickRunnable.beforeDestroy();
            tickRunnable.destroy();
            tickRunnable.afterDestroy();

        } catch (Throwable throwable){
            logger.exception(throwable, "Error occurred while destroying tick #"+id);
        }
        setStatus(TickStatus.DESTROYED);
    }

    private void setStatus(TickStatus status){
        this.status = status;
        logger.info("Tick #"+id+" status changed: "+status.toString());
    }

    @Override
    public String toString() {
        return "Tick { groupId = " + tickGroup.getId() + ", id = " + id + " }";
    }
}
