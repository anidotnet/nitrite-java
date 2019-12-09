package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.NitriteMapper;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryFactory {
    public static <T> ObjectRepository<T> getRepository(Class<T> type,
                                                        String collectionName,
                                                        NitriteConfig nitriteConfig) {
        if (type == null) {
            throw new ValidationException("type cannot be null");
        }

        if (isNullOrEmpty(collectionName)) {
            throw new ValidationException("nitriteCollection cannot be null");
        }

        if (nitriteConfig == null) {
            throw new ValidationException("nitriteContext cannot be null");
        }

        NitriteMapper nitriteMapper = nitriteConfig.nitriteMapper();
        if (nitriteMapper.isValueType(type)) {
            throw new ValidationException("a value type cannot be used to create repository");
        }

        NitriteCollection nitriteCollection = CollectionFactory.getCollection(collectionName, nitriteConfig);
        return new DefaultObjectRepository<>(type, nitriteCollection, nitriteConfig);
    }
}
