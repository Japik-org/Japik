package com.pro100kryto.server.livecycle;

@FunctionalInterface
public interface IGetStatus {
    LiveCycleStatus getStatus() throws NotImplementedLiveCycleOperation;
}
