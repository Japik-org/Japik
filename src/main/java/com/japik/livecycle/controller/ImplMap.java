package com.japik.livecycle.controller;


import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ImplMap<T> {
    private final HashMap<ILiveCycleImplId, Pair<ILiveCycleImplId, T>> map =
            new HashMap<>(8);

    private final AtomicInteger automaticPriority = new AtomicInteger(0);

    public void put(ILiveCycleImplId id, T impl) {
        final Pair<ILiveCycleImplId, T> pair = Tuples.pair(id, impl);
        map.put(id, pair);
    }

    public void put(String idName, int priority, T impl) {
        final ILiveCycleImplId id = new LiveCycleImplId(idName, priority);
        put(id, impl);
    }

    public void putPriorityOrder(String idName, T impl) {
        final ILiveCycleImplId id = new LiveCycleImplId(idName, automaticPriority.incrementAndGet());
        put(id, impl);
    }

    public Pair<ILiveCycleImplId, T> find(String idName) {
        final ILiveCycleImplId id = map.keySet().stream()
                .filter(key -> key.getName().equals(idName))
                .findAny().orElse(null);
        return map.get(id);
    }

    public Collection<Pair<ILiveCycleImplId, T>> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    public Pair<ILiveCycleImplId, T> remove(String idName) {
        final ILiveCycleImplId id = map.keySet().stream()
                .filter(key -> key.getName().equals(idName))
                .findAny().orElse(null);
        return map.remove(id);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map.clear();
    }
}
