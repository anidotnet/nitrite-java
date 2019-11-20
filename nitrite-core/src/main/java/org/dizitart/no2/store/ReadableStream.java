package org.dizitart.no2.store;

import org.dizitart.no2.common.util.Iterables;

import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
public interface ReadableStream<T> extends Iterable<T> {
    default long size() {
        return toList().size();
    }

    default List<T> toList() {
        return Iterables.toList(this);
    }
}
