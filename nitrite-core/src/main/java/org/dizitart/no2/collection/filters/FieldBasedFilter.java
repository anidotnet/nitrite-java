package org.dizitart.no2.collection.filters;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.collection.operation.IndexOperations;
import org.dizitart.no2.mapper.NitriteMapper;

/**
 * @author Anindya Chatterjee.
 */
@Getter
@Setter(AccessLevel.PROTECTED)
public abstract class FieldBasedFilter implements Filter {
    private Field field;
    private Object value;
    private NitriteMapper nitriteMapper;

    @Setter(AccessLevel.PUBLIC)
    private IndexOperations indexOperations;

    @Setter(AccessLevel.PUBLIC)
    private Indexer indexer;

    protected FieldBasedFilter(Field field, Object value) {
        this.field = field;
        this.value = value;
    }
}
