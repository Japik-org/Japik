package com.pro100kryto.server.livecycle;

import org.jetbrains.annotations.Nullable;

public final class LiveCycleStatusBasic {
    private final String name;

    public static final LiveCycleStatusBasic NOT_INITIALIZED = new LiveCycleStatusBasic("NOT_INITIALIZED");
    public static final LiveCycleStatusBasic INITIALIZED = new LiveCycleStatusBasic("INITIALIZED");
    public static final LiveCycleStatusBasic STARTED = new LiveCycleStatusBasic("STARTED");
    public static final LiveCycleStatusBasic BROKEN = new LiveCycleStatusBasic("BROKEN");

    private LiveCycleStatusBasic(String name) {
        this.name = name;
    }

    public boolean is(LiveCycleStatusBasic status){
        return this == status;
    }

    // ----------------

    public boolean isNotInitialized(){
        return is(NOT_INITIALIZED);
    }

    public boolean isInitialized(){
        return is(INITIALIZED) || is(STARTED);
    }

    public boolean isStarted(){
        return is(STARTED);
    }

    public boolean isBroken(){
        return is(BROKEN);
    }

    // ----------------

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            return obj.toString().equals(name) && (obj instanceof LiveCycleStatusBasic);
        } catch (NullPointerException ignored){
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
