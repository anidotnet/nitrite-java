package org.dizitart.no2.sync.message;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.sync.crdt.LastWriteWinState;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
public interface ReceiptAware extends DataGateMessage {
    LastWriteWinState getFeed();

    default Receipt calculateReceipt() {
        Set<Long> added = new HashSet<>();
        Set<Long> removed = new HashSet<>();

        if (getFeed() != null) {
            if (getFeed().getChanges() != null) {
                for (Document change : getFeed().getChanges()) {
                    added.add(change.getId().getIdValue());
                }
            }

            if (getFeed().getTombstones() != null) {
                for (Map.Entry<Long, Long> entry : getFeed().getTombstones().entrySet()) {
                    removed.add(entry.getKey());
                }
            }
        }

        return new Receipt(added, removed);
    }
}
