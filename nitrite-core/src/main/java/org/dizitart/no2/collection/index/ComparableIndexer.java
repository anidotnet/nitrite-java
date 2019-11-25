package org.dizitart.no2.collection.index;

import org.dizitart.no2.NitriteId;

/**
 * @author Anindya Chatterjee
 */
public abstract class ComparableIndexer implements Indexer {

    public abstract NitriteId findByEqual(Object value);
}
