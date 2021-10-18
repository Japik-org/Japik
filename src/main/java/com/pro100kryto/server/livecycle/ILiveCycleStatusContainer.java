package com.pro100kryto.server.livecycle;

public interface ILiveCycleStatusContainer {
    boolean is(LiveCycleStatusBasic status);
    boolean is(LiveCycleStatusAdvanced status);

    /**
     *
     * @return true if status equal NOT_INITIALIZED. Return false if is broken!
     */
    boolean isNotInitialized();

    /**
     * @return true if is initialized or started
     */
    boolean isInitialized();
    boolean isStarted();

    boolean isBroken();

    boolean isFinal();

    LiveCycleStatusBasic getBasic();
    ILiveCycleStatusAdvanced getAdvanced();

    boolean isStopAnnounced();
}
