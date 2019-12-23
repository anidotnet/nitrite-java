package org.dizitart.no2.rx;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
class Pair<K, V> {
    private K key;
    private V value;
}
