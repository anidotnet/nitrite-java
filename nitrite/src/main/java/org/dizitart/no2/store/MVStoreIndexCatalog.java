package org.dizitart.no2.store;

import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.index.IndexEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dizitart.no2.common.Constants.*;

/**
 * @author Anindya Chatterjee
 */
class MVStoreIndexCatalog implements IndexCatalog {
    private NitriteStore nitriteStore;

    MVStoreIndexCatalog(NitriteStore nitriteStore) {
        this.nitriteStore = nitriteStore;
    }

    @Override
    public boolean hasIndexEntry(String collectionName, Field field) {
        NitriteMap<Field, IndexMeta> indexMetaMap = getIndexMetaMap(collectionName);
        if (!indexMetaMap.containsKey(field)) return false;

        IndexMeta indexMeta = indexMetaMap.get(field);
        return indexMeta != null;
    }

    @Override
    public IndexEntry createIndexEntry(String collectionName, Field field, String indexType) {
        IndexEntry index = new IndexEntry(indexType, field, collectionName);

        IndexMeta indexMeta = new IndexMeta();
        indexMeta.setIndex(index);
        indexMeta.setIsDirty(new AtomicBoolean(false));
        indexMeta.setIndexMap(getIndexMapName(index));

        getIndexMetaMap(collectionName).put(field, indexMeta);

        return index;
    }

    @Override
    public IndexEntry findIndexEntry(String collectionName, Field field) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(field);
        if (meta != null) {
            return meta.getIndex();
        }
        return null;
    }

    @Override
    public boolean isDirtyIndex(String collectionName, Field field) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(field);
        return meta != null && meta.getIsDirty().get();
    }

    @Override
    public Collection<IndexEntry> listIndexEntries(String collectionName) {
        Set<IndexEntry> indexSet = new LinkedHashSet<>();
        for (IndexMeta indexMeta : getIndexMetaMap(collectionName).values()) {
            indexSet.add(indexMeta.getIndex());
        }
        return Collections.unmodifiableSet(indexSet);
    }

    @Override
    public void dropIndexEntry(String collectionName, Field field) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(field);
        if (meta != null && meta.getIndex() != null) {
            String indexMapName = meta.getIndexMap();
            nitriteStore.openMap(indexMapName).drop();
        }
        getIndexMetaMap(collectionName).remove(field);
    }

    @Override
    public void beginIndexing(String collectionName, Field field) {
        markDirty(collectionName, field, true);
    }

    @Override
    public void endIndexing(String collectionName, Field field) {
        markDirty(collectionName, field, false);
    }

    private NitriteMap<Field, IndexMeta> getIndexMetaMap(String collectionName) {
        String indexMetaName = getIndexMetaName(collectionName);
        return nitriteStore.openMap(indexMetaName);
    }

    private String getIndexMetaName(String collectionName) {
        return INDEX_META_PREFIX + INTERNAL_NAME_SEPARATOR + collectionName;
    }

    private String getIndexMapName(IndexEntry index) {
        return  INDEX_PREFIX +
            INTERNAL_NAME_SEPARATOR +
            index.getCollectionName() +
            INTERNAL_NAME_SEPARATOR +
            index.getField() +
            INTERNAL_NAME_SEPARATOR +
            index.getIndexType();
    }

    private void markDirty(String collectionName, Field field, boolean dirty) {
        IndexMeta meta = getIndexMetaMap(collectionName).get(field);
        if (meta != null && meta.getIndex() != null) {
            meta.getIsDirty().set(dirty);
        }
    }
}
