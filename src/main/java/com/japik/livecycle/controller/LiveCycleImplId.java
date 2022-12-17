package com.japik.livecycle.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
public final class LiveCycleImplId implements ILiveCycleImplId {
    private final String name;
    @Setter
    private int priority;

    public LiveCycleImplId(String name) {
        this.name = name;
        this.priority = LiveCycleController.PRIORITY_NORMAL;
    }

    public LiveCycleImplId(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
}
