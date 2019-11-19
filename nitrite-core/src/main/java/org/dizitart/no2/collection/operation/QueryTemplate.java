package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 */
class QueryTemplate {
    QueryTemplate(String collectionName, IndexTemplate indexTemplate, NitriteConfig nitriteConfig, NitriteStore nitriteStore) {

    }

    public DocumentCursor find(Filter filter) {
        return null;
    }

    public DocumentCursor find() {
        return null;
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
