package org.dizitart.no2.collection.index;

/**
 * @author Anindya Chatterjee.
 */
public class UniqueIndexer extends ComparableIndexer {
    @Override
    public String getIndexType() {
        return IndexType.Unique;
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}
