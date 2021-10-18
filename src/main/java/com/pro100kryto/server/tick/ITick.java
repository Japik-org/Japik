package com.pro100kryto.server.tick;


public interface ITick {
    long getId();
    TickStatus getStatus();

    // ---

    /**
     * @throws IllegalStateException
     */
    void init();

    /**
     * @throws IllegalStateException
     */
    void activate();

    /**
     * @throws IllegalStateException
     */
    void inactivate();

    /**
     * @throws IllegalStateException
     */
    void destroy();

    // -----

    ITickSettings getTickSettings();
}
