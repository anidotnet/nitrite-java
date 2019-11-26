package org.dizitart.no2.collection.index;

/**
 * @author Anindya Chatterjee
 */
public class NonUniqueIndexer extends ComparableIndexer {

    @Override
    boolean isUnique() {
        return false;
    }

    @Override
    public String getIndexType() {
        return IndexType.NonUnique;
    }
}
