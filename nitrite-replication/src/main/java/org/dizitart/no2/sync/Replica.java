package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinRegister;

/**
 * @author Anindya Chatterjee
 */
public class Replica implements CollectionEventListener, EventListener {
    private LastWriteWinMap<NitriteId, Document> crdt;
    private NitriteCollection collection;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(NitriteCollection collection,
            NitriteMap<NitriteId, LastWriteWinRegister<Document>> nitriteMap) {
        this.collection = collection;
        this.crdt = new LastWriteWinMap<>(nitriteMap);
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        Document document = (Document) eventInfo.getItem();
        switch (eventInfo.getEventType()) {
            case Insert:
            case Update:
                crdt.put(document.getId(), document, System.currentTimeMillis());
                break;
            case Remove:
                crdt.remove(document.getId(), System.currentTimeMillis());
                break;
            case IndexStart:
            case IndexEnd:
                // nothing required
                break;
        }
    }


    @Override
    public void onEvent(ReplicationEvent event) {

    }
}
