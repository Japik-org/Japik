package com.japik.module.events;

public class ModuleConnectionOpenEvent implements IModuleConnectionEvent {
    @Override
    public int getEventType() {
        return ModuleConnectionEventType.OPEN;
    }
}
