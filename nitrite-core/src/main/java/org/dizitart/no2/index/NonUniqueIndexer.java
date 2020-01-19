package org.dizitart.no2.index;

/**
 * @author Anindya Chatterjee
 */
public final class NonUniqueIndexer extends ComparableIndexer {

    @Override
    boolean isUnique() {
        return false;
    }

    @Override
    public String getIndexType() {
        return IndexType.NonUnique;
    }
}
