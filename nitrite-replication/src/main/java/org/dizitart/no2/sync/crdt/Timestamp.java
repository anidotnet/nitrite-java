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

    public static int compare(Timestamp ts1, Timestamp ts2) {
        if(ts1.clock < ts2.clock) return -1;
        else if(ts1.clock > ts2.clock) return 1;
        else {
            return Long.compare(ts1.timestamp, ts2.timestamp);
        }
    }
}
