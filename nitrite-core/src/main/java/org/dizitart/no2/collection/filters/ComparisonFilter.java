package org.dizitart.no2.collection.filters;

import org.dizitart.no2.collection.Field;

/**
 * @author Anindya Chatterjee
 */
abstract class ComparisonFilter extends IndexAwareFilter {
    protected ComparisonFilter(Field field, Comparable<?> value) {
        super(field, value);
    }

    @SuppressWarnings("rawtypes")
    public Comparable getComparable() {
        return (Comparable) getValue();
    }
}
