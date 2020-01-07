package org.dizitart.no2.store;

import lombok.Data;
import org.dizitart.no2.index.IndexEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
@Data
public class IndexMeta implements Serializable {
    private static final long serialVersionUID = 1576690663L;

    private IndexEntry indexEntry;
    private String indexMap;
    private AtomicBoolean isDirty;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(indexEntry);
        stream.writeUTF(indexMap);
        stream.writeObject(isDirty);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        indexEntry = (IndexEntry) stream.readObject();
        indexMap = stream.readUTF();
        isDirty = (AtomicBoolean) stream.readObject();
    }
}
