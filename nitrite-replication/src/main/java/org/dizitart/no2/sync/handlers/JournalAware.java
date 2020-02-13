package org.dizitart.no2.sync.handlers;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.sync.FeedJournal;
import org.dizitart.no2.sync.MessageFactory;
import org.dizitart.no2.sync.MessageTemplate;
import org.dizitart.no2.sync.ReplicationTemplate;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.DataGateFeed;
import org.dizitart.no2.sync.message.Receipt;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Anindya Chatterjee
 */
public interface JournalAware {
    ReplicationTemplate getReplicationTemplate();

    default FeedJournal getJournal() {
        return getReplicationTemplate().getFeedJournal();
    }

    default void retryFailed(Receipt receipt) {
        if (shouldRetry(receipt)) {
            LastWriteWinState state = createState(receipt);

            MessageFactory factory = getReplicationTemplate().getMessageFactory();
            DataGateFeed feedMessage = factory.createFeedMessage(getReplicationTemplate().getConfig(),
                getReplicationTemplate().getReplicaId(), state);

            MessageTemplate messageTemplate = getReplicationTemplate().getMessageTemplate();
            messageTemplate.sendMessage(feedMessage);
        }
    }

    default LastWriteWinState createState(Receipt receipt) {
        LastWriteWinState state = new LastWriteWinState();
        state.setTombstones(new HashMap<>());
        state.setChanges(new HashSet<>());

        NitriteCollection collection = getReplicationTemplate().getCollection();
        LastWriteWinMap crdt = getReplicationTemplate().getCrdt();

        if (receipt != null) {
            if (receipt.getAdded() != null) {
                for (Long id : receipt.getAdded()) {
                    Document document = collection.getById(NitriteId.createId(id));
                    if (document != null) {
                        state.getChanges().add(document);
                    }
                }
            }

            if (receipt.getRemoved() != null) {
                for (Long id : receipt.getRemoved()) {
                    Long timestamp = crdt.getTombstones().get(NitriteId.createId(id));
                    if (timestamp != null) {
                        state.getTombstones().put(id, timestamp);
                    }
                }
            }
        }

        return state;
    }

    default boolean shouldRetry(Receipt receipt) {
        if (receipt == null) return false;
        if (receipt.getAdded() == null && receipt.getRemoved() == null) return false;
        return !receipt.getAdded().isEmpty() || !receipt.getRemoved().isEmpty();
    }
}
