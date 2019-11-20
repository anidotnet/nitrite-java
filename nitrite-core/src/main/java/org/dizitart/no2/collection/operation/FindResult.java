package org.dizitart.no2.collection.operation;

import lombok.Data;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.store.NitriteStore;

import java.util.Set;

/**
 * @author Anindya Chatterjee.
 */
@Data
class FindResult {
    private Boolean hasMore;
    private Long totalCount;
    private Set<NitriteId> idSet;
    private String collectionName;
    private NitriteStore nitriteStore;
}

