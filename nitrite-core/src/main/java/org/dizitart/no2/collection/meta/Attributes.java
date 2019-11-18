package org.dizitart.no2.collection.meta;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents metadata attributes of a collection.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class Attributes implements Serializable {
    private static final long serialVersionUID = 1481284930L;

    public static final String CREATED_TIME = "createdTime";
    public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
    public static final String OWNER = "owner";
    public static final String UUID = "uuid";

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public Attributes(String collection) {
        set(OWNER, collection);
        set(CREATED_TIME, System.currentTimeMillis());
        set(UUID, java.util.UUID.randomUUID().toString());
    }

    private Attributes() {
        // constructor for jackson
    }

    public Attributes set(String key, Object value) {
        attributes.put(key, value);
        return this;
    }
}
