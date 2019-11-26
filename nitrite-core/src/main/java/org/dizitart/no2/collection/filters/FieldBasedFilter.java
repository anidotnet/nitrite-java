package org.dizitart.no2.collection.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dizitart.no2.collection.Field;

/**
 * @author Anindya Chatterjee
 */
@Data
@EqualsAndHashCode(callSuper = true)
abstract class FieldBasedFilter extends NitriteFilter {
    private Field field;
    private Object value;

    protected FieldBasedFilter(Field field, Object value) {
        this.field = field;
        this.value = value;
    }
}
