package org.dizitart.no2.sync.crdt;

import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
}
