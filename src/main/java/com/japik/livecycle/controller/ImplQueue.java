package com.japik.livecycle.controller;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import java.util.Comparator;
import java.util.PriorityQueue;

public final class ImplQueue<T> {
    private final PriorityQueue<Pair<ILiveCycleImplId, T>> queue =
            new PriorityQueue<>(8, Comparator.comparingInt(value -> value.getOne().getPriority()));

    public void put(ILiveCycleImplId id, T impl) {
        remove(id.getName());
        queue.add(Tuples.pair(id, impl));
    }

    public Pair<ILiveCycleImplId, T> find(String idName) {
        return queue.stream()
                .filter(pair -> pair.getOne().getName().equals(idName))
                .findAny().orElse(null);
    }

    public void remove(Pair<ILiveCycleImplId, T> pair) {
        queue.remove(pair);
    }

    public Pair<ILiveCycleImplId, T> remove(String idName) {
        final Pair<ILiveCycleImplId, T> pair = find(idName);
        queue.remove(pair);
        return pair;
    }

    public void clear() {
        queue.clear();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public Pair<ILiveCycleImplId, T> peek() {
        return queue.peek();
    }

    public Pair<ILiveCycleImplId, T> poll() {
        return queue.poll();
    }
}
