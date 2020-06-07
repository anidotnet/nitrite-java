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
        Set<String> added = new HashSet<>();
        Set<String> removed = new HashSet<>();

        if (getFeed() != null) {
            if (getFeed().getChanges() != null) {
                for (Document change : getFeed().getChanges()) {
                    added.add(change.getId().getIdValue());
                }
            }

            if (getFeed().getTombstones() != null) {
                for (Map.Entry<String, Long> entry : getFeed().getTombstones().entrySet()) {
                    removed.add(entry.getKey());
                }
            }
        }

        return new Receipt(added, removed);
    }
}
