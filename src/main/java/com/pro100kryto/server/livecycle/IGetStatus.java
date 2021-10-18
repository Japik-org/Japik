package com.pro100kryto.server.livecycle;

@FunctionalInterface
public interface IGetStatus {
    LiveCycleStatusContainer getStatus() throws NotImplementedLiveCycleOperation;
}
