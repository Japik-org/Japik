package com.pro100kryto.server.tick;

public interface ITickSettings {
    long getMaxTicksCount();
    void setMaxTicksCount(long maxCount);

    long getDelay();
    void setDelay(long delay);

    long getTicksCount();
    float getTicksPerSec();
}
