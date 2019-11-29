package org.dizitart.no2.collection.filters;

import lombok.*;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.IndexEntry;
import org.dizitart.no2.collection.index.Indexer;
import org.dizitart.no2.store.IndexCatalog;

import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString(exclude = "indexedIdSet")
@EqualsAndHashCode(callSuper = true)
abstract class IndexAwareFilter extends FieldBasedFilter {
    @Getter(AccessLevel.NONE)
    private Set<NitriteId> indexedIdSet;

    private Boolean isFieldIndexed = false;

    protected IndexAwareFilter(Field field, Object value) {
        super(field, value);
    }

    protected abstract Set<NitriteId> calculateIndexedIds();

    public Set<NitriteId> getIndexedIdSet() {
        if (indexedIdSet == null) {
            indexedIdSet = calculateIndexedIds();
        }
        return indexedIdSet;
    }

    protected Indexer getIndexer(Field field) {
        IndexCatalog indexCatalog = getNitriteConfig().getNitriteStore().getIndexCatalog();
        IndexEntry indexEntry = indexCatalog.findIndexEntry(getCollectionName(), field);
        String indexType = indexEntry.getIndexType();

        Set<Indexer> indexers = getNitriteConfig().getIndexers();
        for (Indexer indexer : indexers) {
            if (indexer.getIndexType().equalsIgnoreCase(indexType)) {
                return indexer;
            }
        }

        return null;
    }
}
