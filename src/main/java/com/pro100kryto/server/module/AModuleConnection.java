package com.pro100kryto.server.module;

import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.events.ModuleConnectionEventType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AModuleConnection implements IModuleConnection {
    protected final ILogger logger;
    protected final String moduleName;
    protected final String moduleType;

    private int listenerCounter = 0;
    protected Map<Integer, EventListenerContainer> listeners = new ConcurrentHashMap<>();

    public AModuleConnection(ILogger logger, String moduleName, String moduleType) {
        this.logger = logger;
        this.moduleName = moduleName;
        this.moduleType = moduleType;
    }

    @Override
    public int addEventListener(IModuleConnectionEventListener listener) {
        return addEventListener(listener, ModuleConnectionEventType.ALL);
    }

    @Override
    public int addEventListener(IModuleConnectionEventListener listener, int eventType) {
        final int key = ++listenerCounter;
        EventListenerContainer container = new EventListenerContainer(listener, eventType);
        listeners.put(key, container);
        return key;
    }

    @Override
    public void callEvent(IModuleConnectionEvent event) throws Throwable{
        final int eventType = event.getEventType();
        final Set<Integer> keys = listeners.keySet();
        for (Integer key: keys){
            try {
                final EventListenerContainer container = listeners.get(key);
                try {
                    if (container.check(eventType)) // this listener can accept the event
                        container.listener.onEvent(event);
                } catch (NullPointerException nullPointerException) {
                    logger.writeError("Listener does not exists anymore" );
                }
            } catch (Throwable th){ // any error
                logger.writeException(th, "Listener error occurred for event of type "+eventType);
                //listeners.remove(key);
            }
        }
    }

    @Override
    public final String getModuleType() {
        return moduleType;
    }

    @Override
    public final String getModuleName() {
        return moduleName;
    }

    @Override
    public boolean ping() {
        return true;
    }

    private static final class EventListenerContainer {
        public final IModuleConnectionEventListener listener;
        public final int bypassType;
        public EventListenerContainer(IModuleConnectionEventListener listener, int bypassType) {
            this.listener = listener;
            this.bypassType = bypassType;
        }
        public final boolean check(int type){
            return bypassType==type || bypassType== ModuleConnectionEventType.ALL;
        }
    }
}
