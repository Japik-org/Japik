package com.japik.livecycle;

@FunctionalInterface
public interface IStopSlow {
    /**
     * Announce preparation for a stop and begin slow stop
     * @throws IllegalStateException if is already stopped
     * @throws StopSlowException
     */
    void stopSlow() throws Throwable;
}
