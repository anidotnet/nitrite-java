package org.dizitart.no2.sync;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventType;
import org.dizitart.no2.sync.message.DataGateFeed;

import java.util.Collections;

import static org.dizitart.no2.common.Constants.REPLICATOR;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
@Data
class ReplicaChangeListener implements CollectionEventListener {
    private ReplicationTemplate replicationTemplate;
    private MessageTemplate messageTemplate;

    public ReplicaChangeListener(ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        try {
            if (!REPLICATOR.equals(eventInfo.getOriginator())) {
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
        } catch (Exception e) {
            log.error("Error while processing collection event", e);
            replicationTemplate.postEvent(new ReplicationEvent(ReplicationEventType.Error, e));
        }
    }

    private void handleRemoveEvent(Document document) {
        LastWriteWinState state = new LastWriteWinState();
        NitriteId nitriteId = document.getId();
        Long deleteTime = document.getLastModifiedSinceEpoch();

        if (replicationTemplate.getCrdt() != null) {
            replicationTemplate.getCrdt().getTombstones().put(nitriteId, deleteTime);
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
        if (replicationTemplate.shouldExchangeFeed() && messageTemplate != null) {
            MessageFactory factory = replicationTemplate.getMessageFactory();
            DataGateFeed feedMessage = factory.createFeedMessage(replicationTemplate.getConfig(), replicationTemplate.getReplicaId(), state);

            FeedJournal journal = replicationTemplate.getFeedJournal();
            messageTemplate.sendMessage(feedMessage);
            journal.write(state);
        }
    }
}
