package org.dizitart.no2.mapper;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.util.ObjectUtils;
import org.dizitart.no2.exceptions.ObjectMappingException;

import java.util.*;

import static org.dizitart.no2.common.util.ObjectUtils.newInstance;

/**
 * @author Anindya Chatterjee.
 */
public class MappableMapper implements NitriteMapper {
    private final Map<Class<?>, TypeConverter<?, ?>> converterMap;
    private final Set<Class<?>> valueTypes;

    public MappableMapper() {
        this.converterMap = new HashMap<>();
        this.valueTypes = new HashSet<>();
        initConverters();
    }

    public MappableMapper(TypeConverter<?, ?>... converters) {
        this.converterMap = new HashMap<>();
        this.valueTypes = new HashSet<>();
        initConverters();
        registerConverters(converters);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Source, Target> Target convertType(Source source, Class<Target> targetClass) {
        if (isValue(source)) {
            Class<?> sourceType = source.getClass();
            TypeConverter<?, ?> typeConverter = this.converterMap.get(sourceType);
            if (typeConverter != null && typeConverter.getSourceType().equals(source.getClass())) {
                Converter<Source, Target> converter = (Converter<Source, Target>) typeConverter.getSourceConverter();
                return converter.convert(source);
            } else {
                return (Target) source;
            }
        }

        Class<?> key = findConverterKey(source.getClass());
        if (key != null) {
            TypeConverter<?, ?> typeConverter = converterMap.get(key);
            if (typeConverter.getSourceType().isAssignableFrom(source.getClass())
                && typeConverter.getTargetType().isAssignableFrom(targetClass)) {
                Converter<Source, Target> converter = (Converter<Source, Target>) typeConverter.getSourceConverter();
                return converter.convert(source);
            }
        }

        key = findConverterKey(targetClass);
        if (key != null) {
            TypeConverter<?, ?> typeConverter = converterMap.get(key);
            if (typeConverter.getTargetType().isAssignableFrom(source.getClass())
                && typeConverter.getSourceType().isAssignableFrom(targetClass)) {
                Converter<Source, Target> converter = (Converter<Source, Target>) typeConverter.getTargetConverter();
                return converter.convert(source);
            }
        }


        if (targetClass.equals(Document.class)) {
            if (Mappable.class.isAssignableFrom(source.getClass())) {
                Mappable mappable = (Mappable) source;
                return (Target) mappable.write(this);
            }
        }

        if (source instanceof Document) {
            if (Mappable.class.isAssignableFrom(targetClass)) {
                Target item = newInstance(targetClass, false);
                if (item == null) return null;

                ((Mappable) item).read(this, (Document) source);
                return item;
            }
        }

        throw new ObjectMappingException("object must implements Mappable");
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

    private void initConverters() {
        registerConverter(new TypeConverter<>(NitriteId.class, Long.class, NitriteId::getIdValue, NitriteId::createId));
        registerConverter(new TypeConverter<>(Date.class, Long.class, Date::getTime, Date::new));
        registerConverter(new TypeConverter<>(Number.class, Number.class, source -> source, target -> target));
        registerConverter(new TypeConverter<>(Boolean.class, Boolean.class, source -> source, target -> target));
        registerConverter(new TypeConverter<>(Character.class, Character.class, source -> source, target -> target));
        registerConverter(new TypeConverter<>(String.class, String.class, source -> source, target -> target));
        registerConverter(new TypeConverter<>(byte[].class, byte[].class, source -> source, target -> target));
        registerConverter(new TypeConverter<>(Enum.class, Enum.class, source -> source, target -> target));
    }

    private <T, R> void registerConverter(TypeConverter<T, R> converter) {
        if (converter != null) {
            if (ObjectUtils.isValueType(converter.getTargetType())) {
                this.valueTypes.add(converter.getSourceType());
            }
            this.converterMap.put(converter.getSourceType(), converter);
        }
    }

    private void registerConverters(TypeConverter<?, ?>[] converters) {
        if (converters != null) {
            for (TypeConverter<?, ?> converter : converters) {
                registerConverter(converter);
            }
        }
    }

    private Class<?> findConverterKey(Class<?> type) {
        if (converterMap.containsKey(type)) return type;
        for (Class<?> aClass : converterMap.keySet()) {
            if (aClass.isAssignableFrom(type)) {
                return aClass;
            }
        }
        return null;
    }
}
