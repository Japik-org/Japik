package com.japik.livecycle.controller;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class ImplQueue<T> {
    private final PriorityQueue<Pair<ILiveCycleImplId, T>> queue =
            new PriorityQueue<>(8, Comparator.comparingInt(value -> value.getOne().getPriority()));

    private final AtomicInteger automaticPriority = new AtomicInteger(0);

    public void put(ILiveCycleImplId id, T impl) {
        remove(id.getName());
        queue.add(Tuples.pair(id, impl));
    }

    public void put(String idName, T impl) {
        final ILiveCycleImplId id = new LiveCycleImplId(idName);
        put(id, impl);
    }

    public void put(String idName, int priority, T impl) {
        final ILiveCycleImplId id = new LiveCycleImplId(idName, priority);
        put(id, impl);
    }

    public void putAutoPriorityOrder(String idName, T impl) {
        final ILiveCycleImplId id = new LiveCycleImplId(idName, automaticPriority.incrementAndGet());
        put(id, impl);
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
