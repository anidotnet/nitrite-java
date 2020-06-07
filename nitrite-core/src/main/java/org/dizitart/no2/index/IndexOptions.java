package org.dizitart.no2.index;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.NitriteCollection;

/**
 * Represents options to apply while creating an index.
 *
 * @author Anindya Chatterjee
 * @see NitriteCollection#createIndex(String, IndexOptions)
 * @since 1.0
 */
public class IndexOptions {

    /**
     * Specifies the type of an index to create.
     *
     * @param indexType type of an index.
     * @returns type of an index to create.
     */
    @Getter
    @Setter
    private String indexType;

    /**
     * Indicates whether an index to be created in a non-blocking
     * way.
     *
     * @param async if set to `true` then the index will be created asynchronously;
     * otherwise create index operation will wait until all existing
     * documents are indexed.
     * @returns `true` if index is to be created asynchronously; otherwise `false`.
     * @see NitriteCollection#createIndex(String, IndexOptions)
     */
    @Getter
    @Setter
    private boolean async = false;

    /**
     * Creates an {@link IndexOptions} with the specified `indexType`. Index creation
     * will be synchronous with this option.
     *
     * @param indexType the type of index to be created.
     * @return a new synchronous index creation option.
     */
    public static IndexOptions indexOptions(String indexType) {
        return indexOptions(indexType, false);
    }

    /**
     * Creates an {@link IndexOptions} with the specified `indexType` and `async` flag.
     *
     * @param indexType the type of index to be created.
     * @param async     if set to `true` then the index would be created asynchronously;
     *                  otherwise synchronously.
     * @return a new index creation option.
     */
    public static IndexOptions indexOptions(String indexType, boolean async) {
        IndexOptions options = new IndexOptions();
        options.setIndexType(indexType);
        options.setAsync(async);
        return options;
    }
}

