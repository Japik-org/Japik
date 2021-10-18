package com.pro100kryto.server.livecycle;

@FunctionalInterface
public interface IStart {
    /**
     * Starting job
     * @throws StartException
     * @throws IllegalStateException
     */
    void start() throws Throwable;
}
