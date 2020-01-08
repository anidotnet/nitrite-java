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
public class Entry<Value> implements Serializable {
    private Value value;
    private long timestamp;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(value);
        stream.writeLong(timestamp);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        value = (Value) stream.readObject();
        timestamp = stream.readLong();
    }
}
