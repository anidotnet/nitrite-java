package org.dizitart.no2.sync.message;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
@Data
public class DataGateFeed implements DataGateMessage, ChangeMessage {
    private MessageHeader messageHeader;
    private LastWriteWinState feed;

    public Receipt getReceipt() {
        Set<Long> added = new HashSet<>();
        Set<Long> removed = new HashSet<>();

        if (feed != null) {
            if (feed.getChanges() != null) {
                for (Document change : feed.getChanges()) {
                    added.add(change.getId().getIdValue());
                }
            }

            if (feed.getTombstones() != null) {
                for (Map.Entry<Long, Long> entry : feed.getTombstones().entrySet()) {
                    removed.add(entry.getKey());
                }
            }
        }

        return new Receipt(added, removed);
    }
}
