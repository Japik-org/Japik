package com.japik.livecycle;

@FunctionalInterface
public interface IGetStatus {
    LiveCycleStatus getStatus() throws NotImplementedLiveCycleOperation;
}
