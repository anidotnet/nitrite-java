package org.dizitart.no2.store;

import lombok.Data;
import org.dizitart.no2.index.IndexEntry;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
@Data
class IndexMeta implements Serializable {
    private IndexEntry index;
    private String indexMap;
    private AtomicBoolean isDirty;
}
