package com.japik.livecycle;

import com.japik.livecycle.controller.ILiveCycleImplId;
import com.japik.livecycle.controller.LiveCycleController;
import lombok.Getter;
import lombok.Setter;

public class EmptyLiveCycleImplId extends EmptyLiveCycleImpl implements ILiveCycleImplId {
    @Getter
    protected final String name = this.getClass().getCanonicalName();
    @Getter @Setter
    protected int priority;

    public EmptyLiveCycleImplId() {
        priority = LiveCycleController.PRIORITY_NORMAL;
    }

    public EmptyLiveCycleImplId(int priority) {
        this.priority = priority;
    }
}
