package com.pro100kryto.server.utils;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

public class TransformedUnmodifiableList<S, E> extends AbstractList<E> {
    private final List<S> sourceElements;
    private final Function<S, E> transformer;

    public TransformedUnmodifiableList(@NotNull List<S> sourceElements, @NotNull Function<S, E> transformer) {
        this.sourceElements = sourceElements;
        this.transformer = transformer;
    }

    @Override
    public int size() {
        return sourceElements.size();
    }

    @Override
    public E get(int index) {
        final S sourceElement = sourceElements.get(index);
        return transformer.apply(sourceElement);
    }
}