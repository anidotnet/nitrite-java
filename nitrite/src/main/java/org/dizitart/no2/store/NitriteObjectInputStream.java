package org.dizitart.no2.store;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.index.IndexEntry;
import org.h2.mvstore.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteObjectInputStream extends ObjectInputStream {
    private static final Map<String, Class<?>> migrationMap = new HashMap<>();

    static {
        migrationMap.put("org.dizitart.no2.Security$UserCredential", UserCredential.class);
        migrationMap.put("org.dizitart.no2.NitriteId", NitriteId.class);
        migrationMap.put("org.dizitart.no2.Index", IndexEntry.class);
        migrationMap.put("org.dizitart.no2.IndexType", IndexType.class);
        migrationMap.put("org.dizitart.no2.internals.IndexMetaService$IndexMeta", IndexMeta.class);
        migrationMap.put("org.dizitart.no2.Document", Document.createDocument().getClass());
    }

    public NitriteObjectInputStream(InputStream stream) throws IOException {
        super(stream);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();

        for (final String oldName : migrationMap.keySet()) {
            if (resultClassDescriptor.getName().equals(oldName)) {
                Class<?> replacement = migrationMap.get(oldName);

                try {
                    resultClassDescriptor = ObjectStreamClass.lookup(replacement);
//                    Field f = resultClassDescriptor.getClass().getDeclaredField("name");
//                    f.setAccessible(true);
//                    f.set(resultClassDescriptor, replacement);
                } catch (Exception e) {
                    log.error("Error while replacing class name." + e.getMessage(), e);
                }
            }
        }

        return resultClassDescriptor;
    }

    static Object readFallback(ClassCastException cce, byte[] data) {
        try {
            if (cce.getMessage().contains("cannot assign instance of org.dizitart.no2.store.NitriteObjectInputStream$IndexType " +
                "to field org.dizitart.no2.index.IndexEntry.indexType of type java.lang.String")) {

            }
        } catch (Throwable t) {
            throw DataUtils.newIllegalArgumentException(
                "Could not deserialize {0}", Arrays.toString(data), t);
        }
        return null;
    }

    enum IndexType {
        /**
         * Specifies an unique index.
         */
        Unique,
        /**
         * Specifies a non unique index.
         */
        NonUnique,
        /**
         * Specifies a fulltext search index.
         */
        Fulltext
    }

}
