package org.dizitart.no2.index;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.filters.FluentFilter;

import java.util.Set;

/**
 * Represents a full-text indexing engine. It scans a document
 * and modifies full-text index entries by decomposing texts of
 * an indexed field, into a set of string tokens. It uses the
 * full-text index to search for a specific text.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see FluentFilter#text(String)
 */
public interface TextIndexer extends Indexer {
    /**
     * Finds matching text using full-text index.
     *
     * @param collectionName collection name
     * @param field the value
     * @param value the value
     * @return the result set
     */
    Set<NitriteId> findText(String collectionName, String field, String value);
}
