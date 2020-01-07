package org.dizitart.no2.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Represents a key and a value pair.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class KeyValuePair<Key, Value> implements Serializable {

    /**
     * The key of the pair.
     *
     * @param key the key to set.
     * @returns the key.
     * */
    private Key key;

    /**
     * The value of the pair.
     *
     * @param value the value to set.
     * @returns the value.
     * */
    private Value value;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(key);
        stream.writeObject(value);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        key = (Key) stream.readObject();
        value = (Value) stream.readObject();
    }
}
