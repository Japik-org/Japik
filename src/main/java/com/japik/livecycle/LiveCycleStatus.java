package com.japik.livecycle;

import lombok.Getter;
import lombok.Synchronized;

@Getter
public final class LiveCycleStatus {
    private final BasicNames basicName;
    private final AdvancedNames advancedName;
    private final boolean stopAnnounced;


    public LiveCycleStatus(LiveCycleStatus status, boolean stopAnnounced) {
        this.advancedName = status.getAdvancedName();
        this.basicName = status.getBasicName();
        this.stopAnnounced = stopAnnounced;
    }

    public LiveCycleStatus(LiveCycleStatus status, AdvancedNames advancedName) {
        this(advancedName, status.stopAnnounced);
    }

    public LiveCycleStatus(AdvancedNames advancedName, boolean stopAnnounced){
        this.advancedName = advancedName;
        this.basicName = convertToBasic(advancedName);
        this.stopAnnounced = stopAnnounced;
    }

    public LiveCycleStatus(AdvancedNames advancedName) {
        this(advancedName, false);
    }

    public LiveCycleStatus() {
        this(AdvancedNames.NOT_INITIALIZED, false);
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
