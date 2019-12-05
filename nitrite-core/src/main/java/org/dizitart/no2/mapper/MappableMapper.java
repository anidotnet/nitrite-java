package org.dizitart.no2.mapper;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.exceptions.ObjectMappingException;

/**
 * @author Anindya Chatterjee.
 */
class MappableMapper implements NitriteMapper {
    private static boolean isValueType(Class<?> retType) {
        if (retType.isPrimitive() && retType != void.class) return true;
        if (Number.class.isAssignableFrom(retType)) return true;
        if (Boolean.class == retType) return true;
        if (Character.class == retType) return true;
        if (String.class == retType) return true;
        if (byte[].class.isAssignableFrom(retType)) return true;
        return Enum.class.isAssignableFrom(retType);
    }

    @Override
    public <T> Document toDocument(T object) {
        if (isValueType(object)) return Document.createDocument();
        throw new ObjectMappingException("object must implements Mappable");
    }

    @Override
    public <T> T toObject(Document document, Class<T> type) {
        throw new ObjectMappingException("object must implements Mappable");
    }

    @Override
    public boolean isValueType(Object object) {
        return isValueType(object.getClass());
    }

    @Override
    public Object convertValue(Object object) {
        return object;
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {

    }
}
