package org.dizitart.no2.store;

import lombok.Data;
import org.dizitart.no2.common.NitriteSerializable;
import org.dizitart.no2.index.IndexEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
@Data
class IndexMeta implements NitriteSerializable {
    private static final long serialVersionUID = 1576690663L;

    private IndexEntry index;
    private String indexMap;
    private AtomicBoolean isDirty;

    @Override
    public void writeObject(ObjectOutputStream stream) throws IOException {
        if (index != null) {

        }
    }

    @Override
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

    }
}
