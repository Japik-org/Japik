package com.japik.livecycle;

@FunctionalInterface
public interface IInit {
    /**
     * Initialize element
     * @throws InitException
     * @throws IllegalStateException
     */
    void init() throws Throwable;
}
