package org.dizitart.no2.collection.operation;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.KeyValuePair;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.IndexAwareFilter;
import org.dizitart.no2.filters.LogicalFilter;
import org.dizitart.no2.filters.NitriteFilter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.Indexer;
import org.dizitart.no2.store.IndexCatalog;
import org.dizitart.no2.store.NitriteMap;

import java.util.List;

/**
 * @author Anindya Chatterjee
 */
class ReadOperations {
    private String collectionName;
    private NitriteConfig nitriteConfig;
    private NitriteMap<NitriteId, Document> nitriteMap;

    ReadOperations(String collectionName,
                   NitriteConfig nitriteConfig,
                   NitriteMap<NitriteId, Document> nitriteMap) {
        this.nitriteMap = nitriteMap;
        this.nitriteConfig = nitriteConfig;
        this.collectionName = collectionName;
    }

    public DocumentCursor find() {
        ReadableStream<NitriteId> readableStream = nitriteMap.keySet();
        return new DocumentCursorImpl(readableStream, nitriteMap);
    }

    public DocumentCursor find(Filter filter) {
        if (filter == null) {
            return find();
        }

        prepareFilter(filter);
        ReadableStream<KeyValuePair<NitriteId, Document>> readableStream = nitriteMap.entries();
        FilteredReadableStream filteredReadableStream = new FilteredReadableStream(readableStream, filter);

        return new DocumentCursorImpl(filteredReadableStream, nitriteMap);
    }

    Document getById(NitriteId nitriteId) {
        return nitriteMap.get(nitriteId);
    }

    private void prepareFilter(Filter filter) {
        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            prepareNitriteFilter(nitriteFilter);

            if (filter instanceof IndexAwareFilter) {
                IndexAwareFilter indexAwareFilter = (IndexAwareFilter) filter;
                prepareIndexedFilter(indexAwareFilter);
            }

            if (filter instanceof LogicalFilter) {
                LogicalFilter logicalFilter = (LogicalFilter) filter;
                prepareLogicalFilter(logicalFilter);
            }
        }
    }

    private void prepareNitriteFilter(NitriteFilter nitriteFilter) {
        nitriteFilter.setNitriteConfig(nitriteConfig);
        nitriteFilter.setCollectionName(collectionName);
    }

    private void prepareLogicalFilter(LogicalFilter logicalFilter) {
        List<Filter> filters = logicalFilter.getFilters();
        for (Filter filter : filters) {
            prepareFilter(filter);
        }
    }

    private void prepareIndexedFilter(IndexAwareFilter indexAwareFilter) {
        String field = indexAwareFilter.getField();

        IndexCatalog indexCatalog = nitriteConfig.getNitriteStore().getIndexCatalog();
        IndexEntry indexEntry = indexCatalog.findIndexEntry(collectionName, field);
        if (indexEntry != null) {
            String indexType = indexEntry.getIndexType();

            Indexer indexer = nitriteConfig.findIndexer(indexType);
            if (indexer != null) {
                indexAwareFilter.setIsFieldIndexed(true);
                indexAwareFilter.setIndexer(indexer);
                indexAwareFilter.cacheIndexedIds();
            }
        }
    }
}
