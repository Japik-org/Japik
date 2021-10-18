package com.pro100kryto.server.livecycle;

@FunctionalInterface
public interface IStopForce {
    /**
     * Immediately release resources. This operation can provoke data loss.
     * @throws IllegalStateException if element is not initialized
     */
    void stopForce();
}
