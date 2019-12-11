package org.dizitart.no2.filters;

import org.dizitart.no2.exceptions.FilterException;

/**
 * @author Anindya Chatterjee
 */
abstract class ComparisonFilter extends IndexAwareFilter {
    protected ComparisonFilter(String field, Comparable<?> value) {
        super(field, value);
    }

    @SuppressWarnings("rawtypes")
    public Comparable getComparable() {
        if (getValue() == null) {
            throw new FilterException("value parameter must not be null.");
        }
        return (Comparable) getValue();
    }
}
