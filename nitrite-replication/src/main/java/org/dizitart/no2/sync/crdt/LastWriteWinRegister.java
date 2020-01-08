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
public class LastWriteWinRegister<T> implements Serializable {
    private Entry<T> state;

    public LastWriteWinRegister(T value, long timestamp) {
        this.state = new Entry<>(value, timestamp);
    }

    public T get() {
        return state.getValue();
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
        return this.state.getTimestamp() <= timestamp;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(state);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        state = (Entry<T>) stream.readObject();
    }
}
