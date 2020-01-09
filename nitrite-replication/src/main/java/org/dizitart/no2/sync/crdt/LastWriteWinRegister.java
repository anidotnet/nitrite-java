package org.dizitart.no2.sync.crdt;

import lombok.Data;
import org.dizitart.no2.collection.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Anindya Chatterjee
 */
@Data
public class LastWriteWinRegister<T extends Document> implements Serializable {
    private T state;

    public LastWriteWinRegister(T value) {
        this.state = value;
    }

    public T get() {
        return state;
    }

    public void set(T value, long timestamp) {
        if (applicable(timestamp)) {
            this.state = new Entry<>(value, timestamp);
        }
    }

    public void merge(Entry<T> state) {
        if (applicable(state.getTimestamp())) {
            this.state = state;
        }
    }

    private boolean applicable(long timestamp) {
        return this.state.getLastModifiedSinceEpoch() <= timestamp;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(state);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        state = (Entry<T>) stream.readObject();
    }
}
