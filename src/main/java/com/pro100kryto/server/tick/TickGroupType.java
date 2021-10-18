package com.pro100kryto.server.tick;

public enum TickGroupType {
    /**
     * New ticks can be created or deleted after starting.
     * TickGroup of this type use synchronization between ticks so is slower.
     */
    FREE_MODIFIABLE,

    /**
     * While a TickGroup is alive, no modifications allowed.
     * TickGroup of this type uses more optimal algorithms without synchronizations so is faster.
     */
    PRE_MODIFIABLE
}
