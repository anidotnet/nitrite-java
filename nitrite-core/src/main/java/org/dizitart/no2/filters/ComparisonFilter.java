package org.dizitart.no2.filters;

/**
 * @author Anindya Chatterjee
 */
abstract class ComparisonFilter extends IndexAwareFilter {
    protected ComparisonFilter(String field, Comparable<?> value) {
        super(field, value);
    }

    @SuppressWarnings("rawtypes")
    public Comparable getComparable() {
        return (Comparable) getValue();
    }
}
