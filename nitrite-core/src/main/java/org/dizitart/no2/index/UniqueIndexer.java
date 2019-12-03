package org.dizitart.no2.index;

/**
 * @author Anindya Chatterjee.
 */
class UniqueIndexer extends ComparableIndexer {
    @Override
    public String getIndexType() {
        return IndexType.Unique;
    }

    @Override
    public boolean isUnique() {
        return true;
    }
}