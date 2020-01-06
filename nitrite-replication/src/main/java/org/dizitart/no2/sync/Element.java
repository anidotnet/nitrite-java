package org.dizitart.no2.sync;

import lombok.Data;

import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
@Data
public class Element<E> {
    private E value;
    private UUID uuid;
}
