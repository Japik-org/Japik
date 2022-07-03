package com.japik.tick;

public abstract class ATickRunnable {
    protected ITickRunnableCallback tick;

    /** initialize or reinitialize, prepare for ticking */
    void init(){}

    void beforeInit(){}
    void afterInit(){}

    /** some action before tick starting */
    void beforeActivate(){}
    /** some action after tick was started */
    void afterActivate(){}
    /** some action before tick pausing */
    void beforeInactivate(){}
    /** some action after tick was paused */
    void afterInactivate(){}

    /** release resources, no more ticks. Can be initialized later. */
    void destroy(){}

    void beforeDestroy(){}
    void afterDestroy(){}

    public abstract void tick(long dtms) throws Throwable;

    // ---------

    public final void setTickRunnableCallback(ITickRunnableCallback tickRunnableCallback) {
        this.tick = tickRunnableCallback;
    }
}
