package org.dizitart.no2.sync.crdt;

import lombok.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Anindya Chatterjee
 */
@Data
public class Timestamp implements Serializable {
    private long timestamp;
    private int clock;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeFloat(timestamp);
        stream.writeInt(clock);
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        timestamp = stream.readLong();
        clock = stream.readInt();
    }
}
