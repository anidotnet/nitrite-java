package org.dizitart.no2.store.compat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteObjectInputStream extends ObjectInputStream {
    private static final Map<String, Class<?>> migrationMap = new HashMap<>();

    static {
        migrationMap.put("org.dizitart.no2.Security$UserCredential", Compat.UserCredential.class);
        migrationMap.put("org.dizitart.no2.NitriteId", Compat.NitriteId.class);
        migrationMap.put("org.dizitart.no2.Index", Compat.Index.class);
        migrationMap.put("org.dizitart.no2.IndexType", Compat.IndexType.class);
        migrationMap.put("org.dizitart.no2.internals.IndexMetaService$IndexMeta", Compat.IndexMeta.class);
        migrationMap.put("org.dizitart.no2.Document", Compat.Document.class);
        migrationMap.put("org.dizitart.no2.meta.Attributes", Compat.Attributes.class);
    }

    public NitriteObjectInputStream(InputStream stream) throws IOException {
        super(stream);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();

        for (final String oldName : migrationMap.keySet()) {
            if (resultClassDescriptor != null && resultClassDescriptor.getName().equals(oldName)) {
                Class<?> replacement = migrationMap.get(oldName);

                try {
                    resultClassDescriptor = ObjectStreamClass.lookup(replacement);
                } catch (Exception e) {
                    log.error("Error while replacing class name." + e.getMessage(), e);
                }
            }
        }

        return resultClassDescriptor;
    }
}
