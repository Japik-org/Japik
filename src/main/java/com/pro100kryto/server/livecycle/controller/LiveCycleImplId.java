package com.pro100kryto.server.livecycle.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public final class LiveCycleImplId implements ILiveCycleImplId {
    private final String name;
    @Setter
    private int priority;
}
