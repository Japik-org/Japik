package com.pro100kryto.server.livecycle;

import org.jetbrains.annotations.Nullable;

public final class LiveCycleStatusAdvanced implements ILiveCycleStatusAdvanced {
    private final String name;
    private boolean stopAnnounced = true;

    public static final LiveCycleStatusAdvanced NOT_INITIALIZED = new LiveCycleStatusAdvanced("NOT_INITIALIZED");
    public static final LiveCycleStatusAdvanced INITIALIZING = new LiveCycleStatusAdvanced("INITIALIZING");
    public static final LiveCycleStatusAdvanced INITIALIZED = new LiveCycleStatusAdvanced("INITIALIZED");
    public static final LiveCycleStatusAdvanced STARTING = new LiveCycleStatusAdvanced("STARTING");
    public static final LiveCycleStatusAdvanced STARTED = new LiveCycleStatusAdvanced("STARTED");
    public static final LiveCycleStatusAdvanced STOPPING_SLOW = new LiveCycleStatusAdvanced("STOPPING_SLOW");
    public static final LiveCycleStatusAdvanced STOPPING_FORCE = new LiveCycleStatusAdvanced("STOPPING_FORCE");
    public static final LiveCycleStatusAdvanced STOPPED = new LiveCycleStatusAdvanced("STOPPED");
    public static final LiveCycleStatusAdvanced DESTROYED = new LiveCycleStatusAdvanced("DESTROYED");
    public static final LiveCycleStatusAdvanced BROKEN = new LiveCycleStatusAdvanced("BROKEN");

    private LiveCycleStatusAdvanced(String name) {
        this.name = name;
    }

    @Override
    public boolean is(LiveCycleStatusAdvanced status){
        //return toString().equals(status.toString());
        return this == status;
    }

    @Override
    public boolean is(LiveCycleStatusBasic status) {
        return this.convertToBasic().is(status);
    }

    // -----------

    @Override
    public boolean isNotInitialized() {
        return is(NOT_INITIALIZED) || is(INITIALIZING) || is(DESTROYED);
    }

    @Override
    public boolean isInitialized() {
        return !isNotInitialized();
    }

    @Override
    public boolean isStarted() {
        return is(STARTED) || is(STOPPING_SLOW);
    }

    @Override
    public boolean isBroken(){
        return is(BROKEN);
    }

    @Override
    public boolean isFinal() {
        return is(LiveCycleStatusAdvanced.NOT_INITIALIZED)
                || is(LiveCycleStatusAdvanced.INITIALIZED)
                || is(LiveCycleStatusAdvanced.STARTED);
    }

    // -----------

    @Override
    public boolean isStopAnnounced(){
        return stopAnnounced;
    }

    public void setStopAnnounced(boolean stopAnnounced) {
        this.stopAnnounced = stopAnnounced;
    }

    @Override
    public LiveCycleStatusBasic convertToBasic(){
        if (isStarted())
            return LiveCycleStatusBasic.STARTED;

        if (isInitialized())
            return LiveCycleStatusBasic.INITIALIZED;

        if (isBroken())
            return LiveCycleStatusBasic.BROKEN;

        /*
        if (isNotInitialized())
            return LiveCycleStatusBasic.NOT_INITIALIZED;
        */

        return LiveCycleStatusBasic.NOT_INITIALIZED;
    }

    // -----------

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            return obj.toString().equals(name) && (obj instanceof LiveCycleStatusAdvanced);
        } catch (NullPointerException ignored){
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
