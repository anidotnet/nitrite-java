package org.dizitart.no2.repository;

import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.ReadableStream;

import java.text.Collator;

/**
 * A collection of {@link org.dizitart.no2.NitriteId}s of the database records,
 * as a result of a find operation.
 *
 * @author Anindya Chatterjee
 * @see ObjectRepository#find(org.dizitart.no2.collection.filters.Filter)
 * @see ObjectRepository#find()
 * @since 1.0
 */
public interface Cursor<T> extends ReadableStream<T> {
    Cursor<T> sort(Field field, SortOrder sortOrder, Collator collator, NullOrder nullOrder);

    Cursor<T> limit(int offset, int size);

    /**
     * Projects the result of one type into an {@link Iterable} of other type.
     *
     * @param <P>               the type of the target objects.
     * @param projectionType    the projection type.
     * @return `Iterable` of projected objects.
     */
    <P> ReadableStream<P> project(Class<P> projectionType);

    /**
     * Performs a left outer join with a foreign cursor with the specified lookup parameters.
     *
     * It performs an equality match on the localField to the foreignField from the objects of the foreign cursor.
     * If an input object does not contain the localField, the join treats the field as having a value of `null`
     * for matching purposes.
     *
     * @param <Foreign> the type of the foreign object.
     * @param <Joined> the type of the joined object.
     * @param foreignCursor the foreign cursor for the join.
     * @param lookup the lookup parameter for the join operation.
     * @param type the type of the joined record.
     *
     * @return a lazy iterable of joined objects.
     * @since 2.1.0
     */
    <Foreign, Joined> ReadableStream<Joined> join(Cursor<Foreign> foreignCursor, Lookup lookup, Class<Joined> type);

    default Cursor<T> sort(Field field) {
        return sort(field, SortOrder.Ascending);
    }

    default Cursor<T> sort(Field field, SortOrder sortOrder) {
        return sort(field, sortOrder, NullOrder.Default);
    }

    default Cursor<T> sort(Field field, SortOrder sortOrder, Collator collator) {
        return sort(field, sortOrder, collator, NullOrder.Default);
    }

    default Cursor<T> sort(Field field, SortOrder sortOrder, NullOrder nullOrder) {
        return sort(field, sortOrder, null, nullOrder);
    }
}
