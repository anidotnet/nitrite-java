package org.dizitart.no2.filters;

/**
 * @author Anindya Chatterjee
 */
public abstract class StringFilter extends IndexAwareFilter {
    protected StringFilter(String field, Object value) {
        super(field, value);
    }

    public String getStringValue() {
        return (String) getValue();
    }
}
