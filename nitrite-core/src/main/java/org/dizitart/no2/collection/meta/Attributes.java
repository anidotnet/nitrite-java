package org.dizitart.no2.collection.meta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents metadata attributes of a collection.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class Attributes implements Serializable {
    public static final String CREATED_TIME = "createdTime";
    public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
    public static final String OWNER = "owner";
    public static final String UNIQUE_ID = "uuid";
    public static final String LAST_SYNCED = "lastSynced";
    public static final String SYNC_LOCK = "syncLock";
    public static final String EXPIRY_WAIT = "expiryWait";
    public static final String TOMBSTONE = "tombstone";
    public static final String REPLICA = "replica";
    private static final long serialVersionUID = 1481284930L;
    private Map<String, String> attributes;

    public Attributes() {
        attributes = new ConcurrentHashMap<>();
        set(CREATED_TIME, Long.toString(System.currentTimeMillis()));
        set(UNIQUE_ID, java.util.UUID.randomUUID().toString());
    }

    public Attributes(String collection) {
        attributes = new ConcurrentHashMap<>();
        set(OWNER, collection);
        set(CREATED_TIME, Long.toString(System.currentTimeMillis()));
        set(UNIQUE_ID, java.util.UUID.randomUUID().toString());
    }

    public Attributes set(String key, String value) {
        attributes.put(LAST_MODIFIED_TIME, Long.toString(System.currentTimeMillis()));
        attributes.put(key, value);
        return this;
    }

    public String get(String key) {
        return attributes.get(key);
    }

    public boolean hasKey(String key) {
        return attributes.containsKey(key);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(attributes);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        attributes = (Map<String, String>) stream.readObject();
    }
}
