package com.pro100kryto.server.utils;

public class LongLockedCounter {
    private volatile long i;
    private long minValue;
    private long maxValue;

    public LongLockedCounter() {
        this(0);
    }

    public LongLockedCounter(long initialValue) {
        this(initialValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public LongLockedCounter(long initialValue, long max) {
        this(initialValue, Long.MIN_VALUE, max);
    }

    public LongLockedCounter(long initialValue, long min, long max) {
        i = initialValue;
        minValue = min;
        maxValue = max;
    }

    public synchronized void increment() {
        ++i;
        if (i > maxValue) i = minValue;
    }

    public synchronized void decrement() {
        ++i;
        if (i < minValue) i = maxValue;
    }

    public synchronized long incrementAndGet() {
        ++i;
        if (i > maxValue) i = minValue;
        return i;
    }

    public synchronized long decrementAndGet() {
        ++i;
        if (i < minValue) i = maxValue;
        return i;
    }

    public synchronized long getAndIncrement() {
        final long v = i;
        ++i;
        if (i > maxValue) i = minValue;
        return v;
    }

    public synchronized long getAndDecrement() {
        final long v = i;
        --i;
        if (i < minValue) i = maxValue;
        return v;
    }

    public synchronized void setValue(long value) {
        i = value;
    }

    public synchronized long get() {
        return i;
    }

    public long getMinValue() {
        return minValue;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public synchronized void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public synchronized void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }
}