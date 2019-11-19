package org.dizitart.no2.collection;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;

import java.util.Set;

/**
 * An interface to iterate over database {@code find()} results. It provides a
 * mechanism to iterate over all {@link NitriteId}s of the result.
 *
 * [[app-listing]]
 * [source,java]
 * .Example of {@link DocumentCursor}
 * --
 *  // create/open a database
 *  Nitrite db = Nitrite.builder()
 *         .openOrCreate("user", "password");
 *
 *  // create a collection named - test
 *  NitriteCollection collection = db.getCollection("test");
 *
 *  // returns all ids un-filtered
 *  DocumentCursor result = collection.find();
 *
 *  for (Document doc : result) {
 *      // use your logic with the retrieved doc here
 *  }
 *
 *
 * --
 *
 *  @author Anindya Chatterjee
 *  @since 4.0
 */
public interface DocumentCursor extends RecordIterable<Document> {

    /**
     * Gets a lazy iterable containing all the selected keys of the result documents.
     *
     * @param projection the selected keys of a result document.
     * @return a lazy iterable of documents.
     */
    RecordIterable<Document> project(Document projection);

    /**
     * Performs a left outer join with a foreign cursor with the specified lookup parameters.
     *
     * It performs an equality match on the localField to the foreignField from the documents of the foreign cursor.
     * If an input document does not contain the localField, the join treats the field as having a value of `null`
     * for matching purposes.
     *
     * @param foreignCursor the foreign cursor for the join.
     * @param lookup the lookup parameter for the join operation.
     *
     * @return a lazy iterable of joined documents.
     * @since 2.1.0
     */
    RecordIterable<Document> join(DocumentCursor foreignCursor, Lookup lookup);

    /**
     * Gets the set of all {@link NitriteId}s from the {@code find()} results.
     *
     * @return a set of all {@link NitriteId}s.
     * @since 3.3.0
     */
    Set<NitriteId> idSet();
}
