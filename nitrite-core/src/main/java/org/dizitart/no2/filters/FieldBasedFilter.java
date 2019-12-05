package org.dizitart.no2.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Anindya Chatterjee
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class FieldBasedFilter extends NitriteFilter {
    private String field;
    private Object value;

    protected FieldBasedFilter(String field, Object value) {
        this.field = field;
        this.value = value;
    }
}
