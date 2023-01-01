package com.japik.livecycle;

import com.japik.livecycle.controller.ILiveCycleImplId;
import com.japik.livecycle.controller.LiveCycleController;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class AShortLiveCycleImplId extends AShortLiveCycleImpl implements ILiveCycleImplId {
    protected final String name = this.getClass().getCanonicalName();
    @Setter
    protected int priority;

    public AShortLiveCycleImplId() {
        priority = LiveCycleController.PRIORITY_NORMAL;
    }

    public AShortLiveCycleImplId(int priority) {
        this.priority = priority;
    }
}
