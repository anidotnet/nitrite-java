package org.dizitart.no2.sync.crdt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entry<Key, Value> implements Serializable {
    private Key key;
    private Value value;
    private Timestamp timestamp;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(key);
        stream.writeObject(value);
        stream.writeObject(timestamp);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        key = (Key) stream.readObject();
        value = (Value) stream.readObject();
        timestamp = (Timestamp) stream.readObject();
    }
}
