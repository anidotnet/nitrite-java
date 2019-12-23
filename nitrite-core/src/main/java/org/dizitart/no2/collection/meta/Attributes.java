package org.dizitart.no2.collection.meta;

import org.dizitart.no2.common.NitriteSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents metadata attributes of a collection.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class Attributes implements NitriteSerializable {
    private static final long serialVersionUID = 1481284930L;

    public static final String CREATED_TIME = "createdTime";
    public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
    public static final String OWNER = "owner";
    public static final String UUID = "uuid";
    public static final String LAST_SYNCED = "lastSynced";
    public static final String SYNC_LOCK = "syncLock";
    public static final String EXPIRY_WAIT = "expiryWait";

    private Map<String, String> attributes;

    public Attributes() {
        attributes = new ConcurrentHashMap<>();
        set(CREATED_TIME, Long.toString(System.currentTimeMillis()));
        set(UUID, java.util.UUID.randomUUID().toString());
    }

    public Attributes(String collection) {
        attributes = new ConcurrentHashMap<>();
        set(OWNER, collection);
        set(CREATED_TIME, Long.toString(System.currentTimeMillis()));
        set(UUID, java.util.UUID.randomUUID().toString());
    }

    public Attributes set(String key, String value) {
        attributes.put(key, value);
        return this;
    }

    public String get(String key) {
        return attributes.get(key);
    }

    @Override
    public void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(attributes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        attributes = (Map<String, String>) stream.readObject();
    }
}
