package org.dizitart.no2.mapper;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.util.ObjectUtils;
import org.dizitart.no2.exceptions.ObjectMappingException;

/**
 * @author Anindya Chatterjee.
 */
public class MappableMapper implements NitriteMapper {

    @Override
    public <T> Document writeObject(T object) {
        throw new ObjectMappingException("object must implements Mappable");
    }

    @Override
    public <T> T readObject(Document document, Class<T> type) {
        throw new ObjectMappingException("object must implements Mappable");
    }

    @Override
    public boolean isValueType(Object object) {
        return ObjectUtils.isValueType(object.getClass());
    }

    @Override
    public Object convertValue(Object object) {
        if (object instanceof NitriteId) return convertNitriteId(((NitriteId) object));
        return object;
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {

    }

    private Object convertNitriteId(NitriteId nitriteId) {
        return nitriteId.getIdValue();
    }
}
