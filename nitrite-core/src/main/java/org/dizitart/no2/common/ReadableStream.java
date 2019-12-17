package org.dizitart.no2.common;

import org.dizitart.no2.common.util.Iterables;

import java.util.Iterator;
import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
public interface ReadableStream<T> extends Iterable<T> {
    default long size() {
        return Iterables.size(this);
    }

    default List<T> toList() {
        return Iterables.toList(this);
    }

    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    /**
     * Gets the first element of the result or
     * `null` if it is empty.
     *
     * @return the first element or `null`
     */
    default T firstOrNull() {
        return Iterables.firstOrNull(this);
    }

    static <T> ReadableStream<T> fromIterable(Iterable<T> iterable) {
        return iterable::iterator;
    }

    static <T> ReadableStream<T> fromIterator(Iterator<T> iterator) {
        return () -> iterator;
    }
}
