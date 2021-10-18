package com.pro100kryto.server.livecycle;

public interface ILiveCycleStatusAdvanced {
    boolean is(LiveCycleStatusAdvanced status);

    boolean is(LiveCycleStatusBasic status);

    boolean isNotInitialized();
    boolean isInitialized();
    boolean isStarted();

    boolean isStopAnnounced();

    boolean isBroken();
    boolean isFinal();

    LiveCycleStatusBasic convertToBasic();
}
