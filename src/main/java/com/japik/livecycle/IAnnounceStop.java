package com.japik.livecycle;

@FunctionalInterface
public interface IAnnounceStop {
    /**
     * Announce preparation for a stop.
     * This method can limit or cancel beginning jobs and block job launches. So it ensures what a stop operation will not throw any errors and will not freeze.
     * Launching a stop operation is necessary after announceStop.
     * @throws IllegalStateException if is already stopped
     */
    void announceStop() throws NotImplementedLiveCycleOperation;
}
