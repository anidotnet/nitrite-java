package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteStore;

import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorMessage.FILTERED_FIND_OPERATION_FAILED;

/**
 * @author Anindya Chatterjee
 */
class QueryTemplate {
    private String collectionName;
    private IndexTemplate indexTemplate;
    private NitriteStore nitriteStore;

    QueryTemplate(String collectionName, IndexTemplate indexTemplate,
                  NitriteConfig nitriteConfig, NitriteStore nitriteStore) {
        this.collectionName = collectionName;
        this.indexTemplate = indexTemplate;
        this.nitriteStore = nitriteStore;
    }

    public DocumentCursor find(Filter filter) {
        if (filter == null) {
            return find();
        }
        filter.setIndexedTemplate(indexTemplate);
        Set<NitriteId> result;

        try {
            result = filter.apply(collectionName, nitriteStore);
        } catch (FilterException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FilterException(FILTERED_FIND_OPERATION_FAILED, t);
        }

        FindResult findResult = new FindResult();
        findResult.setNitriteStore(nitriteStore);
        findResult.setCollectionName(collectionName);
        if (result != null) {
            findResult.setHasMore(false);
            findResult.setTotalCount((long) result.size());
            findResult.setIdSet(result);
        }

        return new DocumentCursorImpl(findResult);
    }

    public DocumentCursor find() {
        FindResult findResult = new FindResult();
        findResult.setHasMore(false);
        findResult.setTotalCount(nitriteStore.getCollectionSize(collectionName));
        findResult.setIdSet(nitriteStore.getIdSet(collectionName));
        findResult.setNitriteStore(nitriteStore);
        findResult.setCollectionName(collectionName);

        return new DocumentCursorImpl(findResult);
    }

    public DocumentCursor find(FindOptions findOptions) {
        return null;
    }

    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        return null;
    }

    public Document getById(NitriteId nitriteId) {
        return null;
    }
}
