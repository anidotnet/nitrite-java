package org.dizitart.no2.filters;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.index.Indexer;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@ToString(exclude = "indexedIdSet")
@EqualsAndHashCode(callSuper = true)
public abstract class IndexAwareFilter extends FieldBasedFilter {
    @Getter @Setter
    private Boolean isFieldIndexed = false;

    @Getter @Setter
    private Indexer indexer;

    private Set<NitriteId> indexedIdSet;

    protected IndexAwareFilter(String field, Object value) {
        super(field, value);
    }

    protected abstract Set<NitriteId> findIndexedIdSet();
    protected abstract boolean applyNonIndexed(KeyValuePair<NitriteId, Document> element);

    public void cacheIndexedIds() {
        if (indexedIdSet == null) {
            indexedIdSet = findIndexedIdSet();
        }
    }

    @Override
    public boolean apply(KeyValuePair<NitriteId, Document> element) {
        if (isFieldIndexed) {
            return indexedIdSet.contains(element.getKey());
        }
        return applyNonIndexed(element);
    }
}
