package com.japik.livecycle;

import static com.japik.livecycle.LiveCycleStatus.BasicNames.*;

public final class StatusChecker {

    /**
     * @throws IllegalStateException
     */
    public static void checkInit(LiveCycleStatus status){
        if (status.is(INITIALIZED) || status.is(STARTED))
            throw new IllegalStateException("Already initialized");
        //if (status.isBroken()) throw new IllegalStateException("Is broken");
    }

    /**
     * @throws IllegalStateException
     */
    public static void checkStart(LiveCycleStatus status){
        if (status.is(NOT_INITIALIZED)) throw new IllegalStateException("Is not initialized");
        if (status.is(STARTED)) throw new IllegalStateException("Already started");
        //if (status.isBroken()) throw new IllegalStateException("Is broken");
    }

    /**
     * @throws IllegalStateException
     */
    public static void checkStopSlow(LiveCycleStatus status){
        if (!status.is(STARTED)) throw new IllegalStateException("Is not started");
        if (status.is(LiveCycleStatus.AdvancedNames.STOPPING_SLOW) || status.is(LiveCycleStatus.AdvancedNames.STOPPING_FORCE))
            throw new IllegalStateException("Already stopping");
    }

    /**
     * @throws IllegalStateException
     */
    public static void checkStopForce(LiveCycleStatus status){
        if (!status.is(STARTED) && !status.isBroken()) throw new IllegalStateException("Is not started");
    }

    /**
     * @throws IllegalStateException
     */
    public static void checkDestroy(LiveCycleStatus status){
        if (status.is(STARTED)) throw new IllegalStateException("Is started. Stop before destroy.");
        if (status.isNotInitialized()) throw new IllegalStateException("Is not initialized");
    }
}
