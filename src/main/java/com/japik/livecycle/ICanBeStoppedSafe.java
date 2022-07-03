package com.japik.livecycle;

@FunctionalInterface
public interface ICanBeStoppedSafe {
    /**
     * @return true if this element can be stopped slowly without awaiting and stopped forced without problems.
     */
    boolean canBeStoppedSafe() throws NotImplementedLiveCycleOperation;
}
