package com.japik.networking;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;

public final class ProtocolCollection extends AbstractCollection<IProtocol> {
    private final HashMap<String, IProtocol> protocolMap = new HashMap<>();
    private final Object locker = new Object();


    public boolean containsByName(String protocolName) {
        synchronized (locker) {
            return protocolMap.containsKey(protocolName != null ? protocolName.toLowerCase() : null);
        }
    }

    @Nullable
    public IProtocol getByName(String protocolName){
        synchronized (locker) {
            return protocolMap.get(protocolName != null ? protocolName.toLowerCase() : null);
        }
    }

    @Override
    public boolean add(IProtocol protocol) {
        synchronized (locker) {
            if (protocolMap.containsKey(protocol.getName())) {
                throw new IllegalArgumentException("Protocol '" + protocol.getName() + "' already exists.");
            }
            protocolMap.put(protocol.getName(), protocol);
            return true;
        }
    }

    @Nullable
    public IProtocol removeByName(String protocolName) {
        synchronized (locker) {
            return protocolMap.remove(protocolName != null ? protocolName.toLowerCase() : null);
        }
    }

    @Override
    public @NotNull Iterator<IProtocol> iterator() {
        return new Iterator<IProtocol>() {
            private final Iterator<IProtocol> parentIterator = protocolMap.values().iterator();
            @Override
            public boolean hasNext() {
                synchronized (locker) {
                    return parentIterator.hasNext();
                }
            }

            @Override
            public IProtocol next() {
                synchronized (locker) {
                    return parentIterator.next();
                }
            }

            @Override
            public void remove() {
                synchronized (locker) {
                    parentIterator.remove();
                }
            }
        };
    }

    @Override
    public int size() {
        synchronized (locker) {
            return protocolMap.size();
        }
    }
}
