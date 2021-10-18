package com.pro100kryto.server.livecycle;

import static com.pro100kryto.server.livecycle.LiveCycleStatusBasic.*;

public final class StatusChecker {

    /**
     * @throws IllegalStateException
     */
    public static void checkInit(LiveCycleStatusContainer status){
        if (status.is(INITIALIZED) || status.is(LiveCycleStatusBasic.STARTED))
            throw new IllegalStateException("Already initialized");
        if (status.isBroken())
            throw new IllegalStateException("Is broken");
    }

    /**
     * @throws IllegalStateException
     */
    public static void checkStart(LiveCycleStatusContainer status){
        if (status.is(NOT_INITIALIZED)) throw new IllegalStateException("Not initialized");
        if (status.is(STARTED)) throw new IllegalStateException("Already started");
        if (status.isBroken())
            throw new IllegalStateException("Is broken");
    }

    /**
     * @throws IllegalStateException
     */
    public static void checkStopSlow(LiveCycleStatusContainer status){
        if (!status.is(STARTED)) throw new IllegalStateException("Not started");
        if (status.is(LiveCycleStatusAdvanced.STOPPING_SLOW) || status.is(LiveCycleStatusAdvanced.STOPPING_FORCE))
            throw new IllegalStateException("Already stopping");
    }

    /**
     * @throws IllegalStateException
     */
    public static void checkStopForce(LiveCycleStatusContainer status){
        if (!status.is(INITIALIZED) && !status.isBroken()) throw new IllegalStateException("Not initialized");
    }

    /**
     * @throws IllegalStateException
     */
    public static void checkDestroy(LiveCycleStatusContainer status){
        if (status.is(STARTED)) throw new IllegalStateException("Is started. Stop before destroy.");
    }
}
