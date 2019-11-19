package org.dizitart.no2.collection.index;

import org.dizitart.no2.NitriteConfig;

/**
 * @author Anindya Chatterjee.
 */
public class UniqueIndexer implements Indexer {
    @Override
    public String getIndexType() {
        return IndexType.Unique;
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {

    }
}
