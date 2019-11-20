package org.dizitart.no2.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a key and a value pair.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class KeyValuePair<Key, Value> {

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
}
