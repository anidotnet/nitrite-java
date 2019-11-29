package org.dizitart.no2.collection.filters;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.Indexer;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Getter
@ToString(exclude = "indexedIdSet")
@EqualsAndHashCode(callSuper = true)
public abstract class IndexAwareFilter extends FieldBasedFilter {
    private Set<NitriteId> indexedIdSet;
    @Setter
    private Boolean isFieldIndexed = false;
    @Setter
    private Indexer indexer;

    protected IndexAwareFilter(Field field, Object value) {
        super(field, value);
    }

    protected abstract Set<NitriteId> findIndexedIdSet();

    public void cacheIndexedIds() {
        if (indexedIdSet == null) {
            indexedIdSet = findIndexedIdSet();
        }
    }
}
