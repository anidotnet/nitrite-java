package org.dizitart.no2.collection.meta;

import lombok.*;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents metadata attributes of a collection.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Data
@NoArgsConstructor
public class Attributes implements Serializable {
    private static final long serialVersionUID = 1481284930L;

    /**
     * The collection creation timestamp.
     *
     * @param createdTime collection creation timestamp
     * @return the collection creation timestamp.
     * */
    private long createdTime;

    /**
     * The last modified timestamp.
     *
     * @param createdTime last collection modification timestamp
     * @return the last collection modification timestamp.
     * */
    private long lastModifiedTime;

    /**
     * The last replication timestamp.
     *
     * @param createdTime last replication timestamp
     * @return the last replication timestamp.
     * */
    private long lastSynced;

    /**
     * The sync lock data of the collection.
     *
     * @param syncLock the sync lock data
     * @return the sync lock data.
     * */
    private long syncLock;

    /**
     * The sync lock expiration time in milliseconds.
     *
     * @param syncLock the sync lock expiration time in milliseconds
     * @return the sync lock expiration time in milliseconds.
     * */
    private long expiryWait;

    /**
     * The name of the collection associated with this attribute.
     *
     * @param collection the name of the collection
     * @return the name of the collection.
     * */
    private String collection;

    /**
     * The unique identifier of the collection.
     *
     * @param uuid unique identifier of the collection
     * @return the unique identifier of the collection.
     * */
    private String uuid;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private transient Map<String, Object> info;

    /**
     * Instantiates a new {@link Attributes}.
     */
    public Attributes(String collection) {
        this.createdTime = System.currentTimeMillis();
        this.collection = collection;
        this.uuid = UUID.randomUUID().toString();
        this.info = new ConcurrentHashMap<>();
    }

    public Attributes set(String key, Object value) {
        info.put(key, value);
        return this;
    }

    public Object get(String key) {
        return info.get(key);
    }
}
