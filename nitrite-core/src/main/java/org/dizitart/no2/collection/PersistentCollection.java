package org.dizitart.no2.collection;

import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.events.ChangeAware;
import org.dizitart.no2.collection.index.IndexEntry;
import org.dizitart.no2.collection.meta.MetadataAware;

import java.io.Closeable;
import java.util.Collection;

/**
 * The interface Persistent collection.
 *
 * @param <T> the type parameter
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see NitriteCollection
 * @see org.dizitart.no2.collection.objects.ObjectRepository
 */
public interface PersistentCollection<T> extends ChangeAware, MetadataAware, Closeable {
    /**
     * Creates an index on `value`, if not already exists.
     * If `indexOptions` is `null`, it will use default options.
     * <p>
     * The default indexing option is -
     * <p>
     * - `indexOptions.setAsync(false);`
     * - `indexOptions.setIndexType(IndexType.Unique);`
     * <p>
     * [icon="{@docRoot}/note.png"]
     * [NOTE]
     * ====
     * - '_id' value of the document is always indexed. But full text
     * indexing is not supported on '_id' value.
     * - Compound index is not supported.
     * - Indexing on arrays or collection is not supported
     * - Indexing on non-comparable value is not supported
     * ====
     *
     * @param field        the value to be indexed.
     * @param indexOptions index options.
     * @throws org.dizitart.no2.exceptions.IndexingException if an index already exists on `value`.
     * @see IndexOptions
     * @see org.dizitart.no2.collection.index.IndexType
     */
    void createIndex(Field field, IndexOptions indexOptions);

    /**
     * Rebuilds index on `field` if it exists.
     *
     * @param field the value to be indexed.
     * @param isAsync if set to `true`, the indexing will run in background; otherwise, in foreground.
     * @throws org.dizitart.no2.exceptions.IndexingException if the `field` is not indexed.
     */
    void rebuildIndex(Field field, boolean isAsync);

    /**
     * Gets a set of all indices in the collection.
     *
     * @return a set of all indices.
     * @see IndexEntry
     */
    Collection<IndexEntry> listIndices();

    /**
     * Checks if a value is already indexed or not.
     *
     * @param field the value to check.
     * @return `true` if the `value` is indexed; otherwise, `false`.
     */
    boolean hasIndex(Field field);

    /**
     * Checks if indexing operation is currently ongoing for a `field`.
     *
     * @param field the value to check.
     * @return `true` if indexing is currently running; otherwise, `false`.
     */
    boolean isIndexing(Field field);

    /**
     * Drops the index on a `field`.
     *
     * @param field the index of the `field` to drop.
     * @throws org.dizitart.no2.exceptions.IndexingException if indexing is currently running on the `field`.
     * @throws org.dizitart.no2.exceptions.IndexingException if the `field` is not indexed.
     */
    void dropIndex(Field field);

    /**
     * Drops all indices from the collection.
     *
     * @throws org.dizitart.no2.exceptions.IndexingException if indexing is running on any value.
     */
    void dropAllIndices();

    /**
     * Inserts elements into this collection. If the element has an '_id' field,
     * then the value will be used as an unique key to identify the element
     * in the collection. If the element does not have any '_id' field,
     * then nitrite will generate a new {@link NitriteId} and will add it to the '_id'
     * field.
     * <p>
     * If any of the value is already indexed in the collection, then after insertion the
     * index will also be updated.
     * <p>
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Insert}.
     *
     * @param elements an array of element for batch insertion.
     * @return the result of the write operation.
     * @throws org.dizitart.no2.exceptions.ValidationException      if `elements` is `null`.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the '_id' field's value contains `null`.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the '_id' field's value contains non comparable type, i.e. type that does not implement {@link Comparable}.
     * @throws org.dizitart.no2.exceptions.InvalidIdException        if the '_id' field contains value which is not of the same java type as of other element's '_id' field value in the collection.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if the value of '_id' field clashes with the '_id' field of another element in the repository.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if a value of the element is indexed and it violates the unique constraint in the collection(if any).
     * @see NitriteId
     * @see WriteResult
     */
    WriteResult insert(T[] elements);

    /**
     * Updates `element` in the collection. Specified `element` must have an id.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Update}.
     *
     * @param element the element to update.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException      if the `element` is `null`.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the `element` does not have any id.
     */
    default WriteResult update(T element) {
        return update(element, false);
    }

    /**
     * Updates `element` in the collection. Specified `element` must have an id.
     * If the `element` is not found in the collection, it will be inserted only if `upsert`
     * is set to `true`.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Update
     * or {@link org.dizitart.no2.collection.events.ChangeType#Insert}.
     *
     * @param element the element to update.
     * @param insertIfAbsent if set to `true`, `element` will be inserted if not found.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `element` is `null`.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the `element`
     * does not have any id field.
     */
    WriteResult update(T element, boolean insertIfAbsent);

    /**
     * Deletes the `element` from the collection. The `element` must have an id.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Remove}.
     *
     * @param element the element
     * @return the result of the remove operation.
     * @throws org.dizitart.no2.exceptions.NotIdentifiableException if the `element` does not
     * have any id field.
     */
    WriteResult remove(T element);

    /**
     * Gets a single element from the collection by its id. If no element
     * is found, it will return `null`.
     *
     * @param nitriteId the nitrite id
     * @return the unique nitrite id associated with the document.
     * @throws org.dizitart.no2.exceptions.ValidationException if `nitriteId` is `null`.
     */
    T getById(NitriteId nitriteId);

    /**
     * Drops the collection and all of its indices.
     * <p>
     * Any further access to a dropped collection would result into
     * a {@link IllegalStateException}.
     * <p>
     */
    void drop();

    /**
     * Returns `true` if the collection is dropped; otherwise, `false`.
     *
     * @return a boolean value indicating if the collection has been dropped or not.
     */
    boolean isDropped();

    /**
     * Returns `true` if the collection is open; otherwise, `false`.
     *
     * @return a boolean value indicating if the collection has been closed or not.
     */
    boolean isOpen();

    /**
     * Closes the collection for further access. If a collection once closed
     * can only be opened via {@link org.dizitart.no2.Nitrite#getCollection(String)} or
     * {@link org.dizitart.no2.Nitrite#getRepository(Class)} operation.
     * <p>
     * Any access to a closed collection would result into a {@link IllegalStateException}.
     */
    void close();

    /**
     * Returns the name of the {@link PersistentCollection}.
     *
     * @return the name.
     */
    String getName();

    /**
     * Returns the size of the {@link PersistentCollection}.
     *
     * @return the size.
     */
    long size();
}
