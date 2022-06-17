package com.pro100kryto.server.livecycle.controller;


import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public final class ImplMap<T> {
    private final HashMap<ILiveCycleImplId, Pair<ILiveCycleImplId, T>> map =
            new HashMap<>(8);

    public void put(ILiveCycleImplId id, T impl) {
        final Pair<ILiveCycleImplId, T> pair = Tuples.pair(id, impl);
        map.put(id, pair);
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
