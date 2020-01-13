package org.dizitart.no2.sync.crdt;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;

import java.util.*;

/**
 * @author Anindya Chatterjee
 */
@Data
public class LastWriteWinState {
    private Set<Document> changes;
    private Map<NitriteId, Long> tombstones;

    public LastWriteWinState() {
        changes = new LinkedHashSet<>();
        tombstones = new LinkedHashMap<>();
    }

    private LastWriteWinState(Set<Document> changes) {
        this.changes = changes;
    }

    public List<LastWriteWinState> split(int chunkSize) {
        List<LastWriteWinState> list = new ArrayList<>();
        LastWriteWinState currSet = new LastWriteWinState(new HashSet<>());
        for (Document change : changes) {
            if (currSet.changes.size() == chunkSize) {
                list.add(currSet);
                currSet = new LastWriteWinState(new HashSet<>());
            }
            currSet.changes.add(change);
        }
        list.add(currSet);

        // add tombstones to the first element
        if (list.size() > 0) {
            list.get(0).tombstones = tombstones;
        }

        return list;
    }
}
