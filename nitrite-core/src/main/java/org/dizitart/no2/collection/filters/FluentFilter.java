package org.dizitart.no2.collection.filters;

import org.dizitart.no2.collection.Field;

/**
 * @author Anindya Chatterjee.
 */
public final class FluentFilter {
    private Field field;

    public static FluentFilter when(String fieldName) {
        return when(Field.of(fieldName));
    }

    public static FluentFilter when(Field field) {
        FluentFilter filter = new FluentFilter();
        filter.field = field;
        return filter;
    }

    public Filter eq(Object value) {
        return new EqualsFilter(field, value);
    }
}
