package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.WriteResult;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 */
class ReadWriteOperation {
    ReadWriteOperation(String collectionName, IndexTemplate indexTemplate, QueryTemplate queryTemplate, NitriteConfig nitriteConfig, NitriteStore nitriteStore, EventBus<ChangedItem<Document>, ChangeListener> eventBus) {

    }

    public WriteResult insert(Document[] documents) {
        return null;
    }

    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        return null;
    }

    public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
        return null;
    }
}
