package org.dizitart.no2.store.compat.v3;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
class Compat {
    @Data
    static class UserCredential implements Serializable {
        private byte[] passwordHash;
        private byte[] passwordSalt;
    }

    static class Document extends LinkedHashMap<String, Object> implements Serializable {
    }

    @Data
    static class Index implements Serializable {
        private IndexType indexType;
        private String field;
        private String collectionName;
    }

    @Data
    static class IndexMeta implements Serializable {
        private Index index;
        private String indexMap;
        private AtomicBoolean isDirty;
    }

    @Data
    static class Attributes implements Serializable {
        private long createdTime;
        private long lastModifiedTime;
        private long lastSynced;
        private long syncLock;
        private long expiryWait;
        private String collection;
        private String uuid;
    }

    @Data
    static class NitriteId implements Serializable {
        private Long idValue;
    }

    enum IndexType {
        Unique,
        NonUnique,
        Fulltext
    }
}
