package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.DataGateFeed;

import java.util.Collections;

/**
 * @author Anindya Chatterjee
 */
public class ReplicaChangeListener implements CollectionEventListener {
    private ReplicationTemplate replica;
    private MessageTemplate messageTemplate;

    public ReplicaChangeListener(ReplicationTemplate replica, MessageTemplate messageTemplate) {
        this.replica = replica;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        if (replica.shouldExchangeFeed()) {
            switch (eventInfo.getEventType()) {
                case Insert:
                case Update:
                    Document document = (Document) eventInfo.getItem();
                    handleModifyEvent(document);
                    break;
                case Remove:
                    document = (Document) eventInfo.getItem();
                    handleRemoveEvent(document);
                    break;
                case IndexStart:
                case IndexEnd:
                    break;
            }
        }
    }

    private void handleRemoveEvent(Document document) {
        LastWriteWinState state = new LastWriteWinState();
        NitriteId nitriteId = document.getId();
        Long deleteTime = document.getLastModifiedSinceEpoch();

        if (replica.getCrdt() != null) {
            replica.getCrdt().getTombstones().put(nitriteId, deleteTime);
            state.setTombstones(Collections.singletonMap(nitriteId.getIdValue(), deleteTime));
            sendFeed(state);
        }
    }

    private void handleModifyEvent(Document document) {
        LastWriteWinState state = new LastWriteWinState();
        state.setChanges(Collections.singleton(document));
        sendFeed(state);
    }

    private void sendFeed(LastWriteWinState state) {
        MessageFactory factory = replica.getMessageFactory();
        DataGateFeed feedMessage = factory.createFeedMessage(replica.getConfig(), replica.getReplicaId(), state);

        FeedJournal journal = replica.getFeedJournal();
        messageTemplate.sendMessage(feedMessage);
        journal.write(state);
    }
}
