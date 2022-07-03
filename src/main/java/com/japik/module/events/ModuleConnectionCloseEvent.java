package com.japik.module.events;

public class ModuleConnectionCloseEvent implements IModuleConnectionEvent {
    @Override
    public int getEventType() {
        return ModuleConnectionEventType.CLOSE;
    }
}
