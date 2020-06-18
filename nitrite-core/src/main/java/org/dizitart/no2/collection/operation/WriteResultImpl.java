package org.dizitart.no2.collection.operation;

import lombok.ToString;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.WriteResult;

import java.util.*;

/**
 * @author Anindya Chatterjee
 */
@ToString
class WriteResultImpl implements WriteResult {
    private Set<NitriteId> nitriteIds;

    void setNitriteIds(Set<NitriteId> nitriteIds) {
        this.nitriteIds = nitriteIds;
    }

    void addToList(NitriteId nitriteId) {
        if (nitriteIds == null) {
            nitriteIds = new HashSet<>();
        }
        nitriteIds.add(nitriteId);
    }

    public int getAffectedCount() {
        if (nitriteIds == null) return 0;
        return nitriteIds.size();
    }

    @Override
    public Iterator<NitriteId> iterator() {
        Iterator<NitriteId> iterator = nitriteIds == null ? Collections.emptyIterator()
            : nitriteIds.iterator();
        return iterator;
    }
}
