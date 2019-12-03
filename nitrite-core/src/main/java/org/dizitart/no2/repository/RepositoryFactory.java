package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.exceptions.ValidationException;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryFactory {
    public static <T> ObjectRepository<T> getRepository(Class<T> type,
                                                        NitriteCollection nitriteCollection,
                                                        NitriteConfig nitriteConfig) {
        if (type == null) {
            throw new ValidationException("type cannot be null");
        }

        if (nitriteCollection == null) {
            throw new ValidationException("nitriteCollection cannot be null");
        }

        if (nitriteConfig == null) {
            throw new ValidationException("nitriteContext cannot be null");
        }

        return new DefaultObjectRepository<>(type, nitriteCollection, nitriteConfig);
    }
}
