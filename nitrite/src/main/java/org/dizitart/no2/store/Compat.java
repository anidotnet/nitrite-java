package org.dizitart.no2.store;

import lombok.Data;
import org.dizitart.no2.index.IndexEntry;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
class Compat {
    @Data
    static class IndexMetaCompat {
        private IndexEntryCompat index;
        private String indexMap;
        private AtomicBoolean isDirty;

        IndexMeta getIndexMeta() {
            IndexMeta meta = new IndexMeta();
            meta.setIndex(index.getIndexEntry());
            meta.setIndexMap(indexMap);
            meta.setIsDirty(isDirty);
            return meta;
        }
    }

    @Data
    static class IndexEntryCompat {
        private IndexType indexType;
        private String field;
        private String collectionName;

        IndexEntry getIndexEntry() {
            return new IndexEntry(indexType.name(), field, collectionName);
        }
    }

    enum IndexType {
        Unique,
        NonUnique,
        Fulltext
    }
}
