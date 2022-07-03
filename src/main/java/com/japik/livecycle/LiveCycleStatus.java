package com.japik.livecycle;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

import java.util.concurrent.locks.ReentrantLock;

public final class LiveCycleStatus {
    private final ReentrantLock liveCycleLocker;

    @Getter(onMethod_={@Synchronized})
    private BasicNames basicName;
    @Getter(onMethod_={@Synchronized})
    private AdvancedNames advancedName;

    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean stopAnnounced;

    public LiveCycleStatus(){
        this(new ReentrantLock());
    }

    public LiveCycleStatus(ReentrantLock liveCycleLocker) {
        this.liveCycleLocker = liveCycleLocker;
        set(AdvancedNames.NOT_INITIALIZED);
    }

    @Synchronized
    public boolean is(BasicNames basicName){
        return this.basicName == basicName;
    }

    @Synchronized
    public boolean is(AdvancedNames advancedName){
        return this.advancedName == advancedName;
    }

    @Synchronized
    public boolean isNotInitialized() {
        return is(BasicNames.NOT_INITIALIZED);
    }

    @Synchronized
    public boolean isInitialized() {
        return is(BasicNames.INITIALIZED);
    }

    @Synchronized
    public boolean isStarted() {
        return is(BasicNames.STARTED);
    }

    @Synchronized
    public boolean isBroken(){
        return is(BasicNames.BROKEN) || is (AdvancedNames.BROKEN);
    }

    @Synchronized
    public boolean isFinal(){
        return is(AdvancedNames.NOT_INITIALIZED)
                || is(AdvancedNames.INITIALIZED)
                || is(AdvancedNames.STARTED)
                || is(AdvancedNames.STOPPED)
                || is(AdvancedNames.DESTROYED);
    }

    @Synchronized
    public void set(AdvancedNames advancedName){
        liveCycleLocker.lock();
        try {
            this.advancedName = advancedName;
            this.basicName = convertToBasic(advancedName);
        } finally {
            liveCycleLocker.unlock();
        }
    }

    public static BasicNames convertToBasic(AdvancedNames advancedName){
        if (advancedName == AdvancedNames.STARTED ||
                advancedName == AdvancedNames.STOPPING_SLOW ||
                advancedName == AdvancedNames.STOPPING_FORCE)
            return BasicNames.STARTED;

        if (advancedName == AdvancedNames.INITIALIZED ||
                advancedName == AdvancedNames.STARTING ||
                advancedName == AdvancedNames.STOPPED)
            return BasicNames.INITIALIZED;

        if (advancedName == AdvancedNames.BROKEN)
            return BasicNames.BROKEN;

        /*
        if (isNotInitialized())
            return LiveCycleStatusBasic.NOT_INITIALIZED;
        */

        return BasicNames.NOT_INITIALIZED;
    }

    public enum BasicNames{
        NOT_INITIALIZED,
        INITIALIZED,
        STARTED,
        BROKEN
    }

    public enum AdvancedNames{
        NOT_INITIALIZED,
        INITIALIZING,
        INITIALIZED,
        STARTING,
        STARTED,
        STOPPING_SLOW,
        STOPPING_FORCE,
        STOPPED,
        DESTROYED,
        BROKEN
    }

    @Override
    public String toString() {
        return basicName.toString() + " (" + advancedName.toString() + ')';
    }
}
