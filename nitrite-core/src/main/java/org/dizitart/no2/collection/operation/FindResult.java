package org.dizitart.no2.collection.operation;

import lombok.Data;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.ReadableStream;

/**
 * @author Anindya Chatterjee.
 */
@Data
class FindResult {
    private Boolean hasMore;
    private Long totalCount;
    private ReadableStream<NitriteId> idSet;
    private NitriteMap<NitriteId, Document> nitriteMap;
}

