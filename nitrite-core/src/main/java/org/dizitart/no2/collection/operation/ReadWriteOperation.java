package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.store.NitriteStore;

/**
 * @author Anindya Chatterjee
 */
class ReadWriteOperation {
    ReadWriteOperation(IndexTemplate indexTemplate, QueryTemplate queryTemplate, NitriteConfig nitriteConfig, NitriteStore nitriteStore, EventBus<ChangedItem<Document>, ChangeListener> eventBus) {

    }
}
