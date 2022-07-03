package com.japik.livecycle;

@FunctionalInterface
public interface IStart {
    /**
     * Starting job
     * @throws StartException
     * @throws IllegalStateException
     */
    void start() throws Throwable;
}
