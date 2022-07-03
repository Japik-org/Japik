package com.japik.livecycle;

@FunctionalInterface
public interface IDestroy {
    /**
     * Immediately release all resources. After that, can be initialized again.
     * @throws IllegalStateException
     */
    void destroy();
}
