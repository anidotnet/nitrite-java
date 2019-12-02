package org.dizitart.no2.collection.filters;

import org.dizitart.no2.collection.Field;

/**
 * @author Anindya Chatterjee
 */
public abstract class StringFilter extends IndexAwareFilter {
    protected StringFilter(Field field, Object value) {
        super(field, value);
    }

    public String getStringValue() {
        return (String) getValue();
    }
}
