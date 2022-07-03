package com.japik.tick;

public interface ITickSettings {
    long getMaxTicksCount();
    void setMaxTicksCount(long maxCount);

    long getDelay();
    void setDelay(long delay);

    long getTicksCount();
    float getTicksPerSec();
}
