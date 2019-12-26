package org.dizitart.no2.mapper;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.exceptions.ObjectMappingException;

import java.util.*;

import static org.dizitart.no2.common.util.Iterables.asList;
import static org.dizitart.no2.common.util.ObjectUtils.newInstance;

/**
 * @author Anindya Chatterjee.
 */
public class MappableMapper implements NitriteMapper {
    private final Map<Class<?>, TypeConverter<?>> typeConverterMap;
    private final Set<Class<?>> valueTypes;

    public MappableMapper() {
        this(null, null);
    }

    public MappableMapper(TypeConverter<?>... typeConverters) {
        this(null, asList(typeConverters));
    }

    public MappableMapper(Class<?>... valueTypes) {
        this(asList(valueTypes), null);
    }

    public MappableMapper(List<Class<?>> valueTypes, List<TypeConverter<?>> typeConverters) {
        this.typeConverterMap = new HashMap<>();
        this.valueTypes = new HashSet<>();
        init(valueTypes, typeConverters);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Source, Target> Target convert(Source source, Class<Target> type) {
        if (source == null) {
            return null;
        }

        if (isValue(source)) {
            return (Target) source;
        } else {
            if (Document.class.isAssignableFrom(type)) {
                return (Target) convertToDocument(source);
            } else if (source instanceof Document) {
                return convertToObject((Document) source, type);
            }
        }

        throw new ObjectMappingException("object must implements Mappable or register a TypeConverter");
    }

    @SuppressWarnings("unchecked")
    protected <Target> Target convertToObject(Document source, Class<Target> type) {
        if (source == null) {
            return null;
        }

        if (Mappable.class.isAssignableFrom(type)) {
            Target item = newInstance(type, false);
            if (item == null) return null;

            ((Mappable) item).read(this, source);
            return item;
        }

        Class<?> key = findConverterKey(type);
        if (key != null) {
            TypeConverter<?> typeConverter = typeConverterMap.get(key);
            Converter<Document, ?> converter = typeConverter.getTargetConverter();
            return (Target) converter.convert(source, this);
        }

        throw new ObjectMappingException("object must implements Mappable or register a TypeConverter");
    }

    @SuppressWarnings("unchecked")
    protected <Source> Document convertToDocument(Source source) {
        if (source instanceof Mappable) {
            Mappable mappable = (Mappable) source;
            return mappable.write(this);
        }

        Class<?> key = findConverterKey(source.getClass());
        if (key != null) {
            TypeConverter<Source> typeConverter = (TypeConverter<Source>) typeConverterMap.get(key);
            return typeConverter.getSourceConverter().convert(source, this);
        }

        throw new ObjectMappingException("object must implements Mappable or register a TypeConverter");
    }


    @Override
    public boolean isValueType(Class<?> type) {
        if (type.isPrimitive() && type != void.class) return true;
        if (valueTypes.contains(type)) return true;
        for (Class<?> valueType : valueTypes) {
            if (valueType.isAssignableFrom(type)) return true;
        }
        return false;
    }

    @Override
    public boolean isValue(Object object) {
        return isValueType(object.getClass());
    }

    @Override
    public void initialize(NitriteConfig nitriteConfig) {

    }

    private Class<?> findConverterKey(Class<?> type) {
        if (typeConverterMap.containsKey(type)) return type;
        for (Class<?> aClass : typeConverterMap.keySet()) {
            if (aClass.isAssignableFrom(type)) {
                return aClass;
            }
        }
        return null;
    }

    private void init(List<Class<?>> valueTypes, List<TypeConverter<?>> typeConverters) {
        this.valueTypes.add(Number.class);
        this.valueTypes.add(Boolean.class);
        this.valueTypes.add(Character.class);
        this.valueTypes.add(String.class);
        this.valueTypes.add(byte[].class);
        this.valueTypes.add(Enum.class);
        this.valueTypes.add(NitriteId.class);
        this.valueTypes.add(Date.class);

        if (valueTypes != null && !valueTypes.isEmpty()) {
            this.valueTypes.addAll(valueTypes);
        }

        if (typeConverters != null && !typeConverters.isEmpty()) {
            registerConverters(typeConverters);
        }
    }

    private void registerConverters(List<TypeConverter<?>> converters) {
        if (converters != null) {
            for (TypeConverter<?> converter : converters) {
                typeConverterMap.put(converter.getSourceType(), converter);
            }
        }
    }
}
