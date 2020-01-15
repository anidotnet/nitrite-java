package org.dizitart.no2.sync.crdt;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.sync.module.DocumentDeserializer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Anindya Chatterjee
 */
@Data
public class LastWriteWinState {
    @JsonDeserialize(contentUsing = DocumentDeserializer.class)
    private Set<Document> changes;
    private Map<Long, Long> tombstones;

    public LastWriteWinState() {
        changes = new LinkedHashSet<>();
        tombstones = new LinkedHashMap<>();
    }
}
