package org.dizitart.no2.mapper;

import org.dizitart.no2.Document;
import org.dizitart.no2.plugin.NitritePlugin;

import static org.dizitart.no2.common.util.ObjectUtils.newInstance;

/**
 * @author Anindya Chatterjee.
 */
public interface NitriteMapper extends NitritePlugin {
    <T> Document toDocument(T object);

    <T> T toObject(Document document, Class<T> type);

    boolean isValueType(Object object);

    Object convertValue(Object object);

    default <T> T asObject(Document document, Class<T> type) {
        if (Mappable.class.isAssignableFrom(type)) {
            T item = newInstance(type, false);
            if (item == null) return null;

            ((Mappable) item).read(this, document);
            return item;
        }
        return toObject(document, type);
    }

    default <T> Document asDocument(T object) {
        if (object instanceof Mappable) {
            Mappable mappable = (Mappable) object;
            return mappable.write(this);
        }
        return toDocument(object);
    }
}
