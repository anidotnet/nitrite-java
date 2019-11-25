package org.dizitart.no2.collection;

import org.dizitart.no2.Document;
import org.dizitart.no2.collection.filters.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.VE_INSERT_NULL_DOCUMENT;
import static org.dizitart.no2.exceptions.ErrorCodes.VE_INSERT_OTHERS_CONTAINS_NULL;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * Represents a named document collection stored in nitrite database.
 * It persists documents into the database. Each document is associated
 * with a unique {@link org.dizitart.no2.NitriteId} in a collection.
 *
 * A nitrite collection supports indexing. Every nitrite collection is also
 * observable.
 *
 * [[app-listing]]
 * [source,java]
 * .Create a collection
 * --
 * // create/open a database
 * Nitrite db = Nitrite.builder()
 *         .openOrCreate("user", "password");
 *
 * include::/src/docs/asciidoc/examples/collection.adoc[]
 *
 * --
 *
 * @see org.dizitart.no2.collection.events.ChangeAware
 * @see Document
 * @see org.dizitart.no2.NitriteId
 * @see org.dizitart.no2.collection.events.ChangeListener
 * @see org.dizitart.no2.common.event.EventBus
 * @author Anindya Chatterjee
 * @since 1.0
 */
public interface NitriteCollection extends PersistentCollection<Document> {
    /**
     * Insert documents into a collection. If the document contains a '_id' value, then
     * the value will be used as a unique key to identify the document in the collection.
     * If the document does not contain any '_id' value, then nitrite will generate a new
     * {@link org.dizitart.no2.NitriteId} and will add it to the document.
     *
     * If any of the value is already indexed in the collection, then after insertion the
     * index will also be updated.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: These operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Insert}.
     *
     * @param document  the document to insert
     * @param documents other documents to insert in a batch.
     * @return the result of write operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if `document` is `null`.
     * @throws org.dizitart.no2.exceptions.InvalidIdException if the '_id' value contains `null` value.
     * @throws org.dizitart.no2.exceptions.InvalidIdException if the '_id' value contains non comparable type, i.e.
     * type that does not implement {@link Comparable}.
     * @throws org.dizitart.no2.exceptions.InvalidIdException if the '_id' contains value, which is not of the same java
     * type as of other documents' '_id' in the collection.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if the value of '_id' value clashes with the id
     * of another document in the collection.
     * @throws org.dizitart.no2.exceptions.UniqueConstraintException if a value of the document is indexed and it
     * violates the unique constraint in the collection(if any).
     * @see org.dizitart.no2.NitriteId
     * @see WriteResult
     */
    default WriteResult insert(Document document, Document... documents) {
        notNull(document, errorMessage("a null document cannot be inserted", VE_INSERT_NULL_DOCUMENT));
        if (documents != null) {
            containsNull(documents, errorMessage("a null document cannot be inserted",
                VE_INSERT_OTHERS_CONTAINS_NULL));
        }

        List<Document> documentList = new ArrayList<>();
        documentList.add(document);

        if (documents != null && documents.length > 0) {
            Collections.addAll(documentList, documents);
        }

        return insert(documentList.toArray(new Document[0]));
    }

    /**
     * Update documents in the collection.
     *
     * If the `filter` is `null`, it will update all documents in the collection.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Update}.
     *
     * @param filter the filter to apply to select documents from the collection.
     * @param update the modifications to apply.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` document is `null`.
     */
    default WriteResult update(Filter filter, Document update) {
        return update(filter, update, new UpdateOptions());
    }

    /**
     * Updates documents in the collection. Update operation can be customized
     * with the help of `updateOptions`.
     *
     * If the `filter` is `null`, it will update all documents in the collection unless
     * `justOnce` is set to `true` in `updateOptions`.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Update} or {@link org.dizitart.no2.collection.events.ChangeType#Insert}.
     *
     * @param filter        the filter to apply to select documents from the collection.
     * @param update        the modifications to apply.
     * @param updateOptions the update options to customize the operation.
     * @return the result of the update operation.
     * @throws org.dizitart.no2.exceptions.ValidationException if the `update` document is `null`.
     * @throws org.dizitart.no2.exceptions.ValidationException if `updateOptions` is `null`.
     * @see UpdateOptions
     */
    WriteResult update(Filter filter, Document update, UpdateOptions updateOptions);

    /**
     * Removes matching elements from the collection.
     *
     * If the `filter` is `null`, it will remove all objects from the collection.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Remove}.
     *
     * @param filter the filter to apply to select elements from collection.
     * @return the result of the remove operation.
     */
    default WriteResult remove(Filter filter) {
        return remove(filter, new RemoveOptions());
    }

    /**
     * Removes document from a collection. Remove operation can be customized by
     * `removeOptions`.
     *
     * If the `filter` is `null`, it will remove all documents in the collection unless
     * `justOnce` is set to `true` in `removeOptions`.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This operations will notify all {@link org.dizitart.no2.collection.events.ChangeListener}
     * instances registered to this collection with change type
     * {@link org.dizitart.no2.collection.events.ChangeType#Remove}.
     *
     * @param filter the filter to apply to select documents from collection.
     * @param removeOptions the remove options to customize the operations.
     * @return the result of the remove operation.
     */
    WriteResult remove(Filter filter, RemoveOptions removeOptions);

    /**
     * Returns a cursor to all documents in the collection.
     *
     * @return a cursor to all documents in the collection.
     */
    DocumentCursor find();

    /**
     * Applies a filter on the collection and returns a cursor to the
     * selected documents.
     *
     * See {@link Filter} for all available filters.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If there is an index on the value specified in the filter, this operation
     * will take advantage of the index.
     *
     * @param filter the filter to apply to select documents from collection.
     * @return a cursor to all selected documents.
     * @throws org.dizitart.no2.exceptions.ValidationException if `filter` is null.
     * @see Filter
     * @see DocumentCursor#project(Document)
     */
    DocumentCursor find(Filter filter);
}
