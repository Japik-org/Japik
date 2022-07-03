package com.japik.tick;

public interface ITickCallback {

    /**
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    void setTickActive(long id);

    /**
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    void setTickInactive(long id);

    long getId();
}
