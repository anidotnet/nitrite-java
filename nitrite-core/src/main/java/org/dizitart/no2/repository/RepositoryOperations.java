package org.dizitart.no2.repository;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.exceptions.*;
import org.dizitart.no2.index.annotations.Id;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.index.annotations.Indices;
import org.dizitart.no2.index.annotations.InheritIndices;
import org.dizitart.no2.mapper.NitriteMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.dizitart.no2.collection.filters.FluentFilter.when;
import static org.dizitart.no2.common.util.DocumentUtils.skeletonDocument;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
class RepositoryOperations {
    <T> Document toDocument(T object, NitriteMapper nitriteMapper,
                            Field idField, boolean update) {
        Document document = nitriteMapper.asDocument(object);
        if (idField != null) {
            if (idField.getType() == NitriteId.class) {
                try {
                    idField.setAccessible(true);
                    if (idField.get(object) == null) {
                        NitriteId id = document.getId();
                        idField.set(object, id);
                        document.put(idField.getName(), id.getIdValue());
                    } else if (!update) {
                        throw new InvalidIdException("auto generated id should not be set manually");
                    }
                } catch (IllegalAccessException iae) {
                    throw new InvalidIdException("auto generated id value cannot be accessed");
                }
            }
            Object idValue = document.get(idField.getName());
            if (idValue == null) {
                throw new InvalidIdException("id cannot be null");
            }
            if (idValue instanceof String && isNullOrEmpty((String) idValue)) {
                throw new InvalidIdException("id value cannot be empty string");
            }
        }
        return document;
    }

    /**
     * Creates unique filter from the object.
     *
     * @param object  the object
     * @param idField the id field
     * @return the equals filter
     */
    Filter createUniqueFilter(Object object, Field idField) {
        idField.setAccessible(true);
        try {
            Object value = idField.get(object);
            if (value == null) {
                throw new InvalidIdException("id value cannot be null");
            }
            return when(idField.getName()).eq(value);
        } catch (IllegalAccessException iae) {
            throw new InvalidIdException("id field is not accessible");
        }
    }

    <T> Field getIdField(NitriteMapper nitriteMapper, Class<T> type) {
        List<Field> fields;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            fields = getFieldsUpto(type, Object.class);
        } else {
            fields = Arrays.asList(type.getDeclaredFields());
        }

        boolean alreadyIdFound = false;
        Field idField = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                validateObjectIndexField(nitriteMapper, field.getType(), field.getName());
                if (alreadyIdFound) {
                    throw new NotIdentifiableException("multiple id fields found for the type");
                } else {
                    alreadyIdFound = true;
                    idField = field;
                }
            }
        }
        return idField;
    }

    <T> Set<Index> extractIndices(NitriteMapper nitriteMapper, Class<T> type) {
        notNull(type, "type cannot be null");

        List<Indices> indicesList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indicesList = findAnnotations(Indices.class, type);
        } else {
            indicesList = new ArrayList<>();
            Indices indices = type.getAnnotation(Indices.class);
            if (indices != null) indicesList.add(indices);
        }

        Set<Index> indexSet = new LinkedHashSet<>();
        if (indicesList != null) {
            for (Indices indices : indicesList) {
                Index[] indexList = indices.value();
                populateIndex(nitriteMapper, type, Arrays.asList(indexList), indexSet);
            }
        }

        List<Index> indexList;
        if (type.isAnnotationPresent(InheritIndices.class)) {
            indexList = findAnnotations(Index.class, type);
        } else {
            indexList = new ArrayList<>();
            Index index = type.getAnnotation(Index.class);
            if (index != null) indexList.add(index);
        }

        if (indexList != null) {
            populateIndex(nitriteMapper, type, indexList, indexSet);
        }
        return indexSet;
    }

    <T> Field getField(Class<T> type, String name, boolean recursive) {
        if (name.contains(NitriteConfig.getFieldSeparator())) {
            return getEmbeddedField(type, name);
        } else {
            // first check declared fields (fix for kotlin properties, ref: issue #54)
            // if nothing found and is-recursive then check recursively
            Field[] declaredFields = type.getDeclaredFields();
            Field field = null;
            for (Field declaredField : declaredFields) {
                if (declaredField.getName().equals(name)) {
                    field = declaredField;
                    break;
                }
            }

            if (field == null && recursive) {
                List<Field> fields = getFieldsUpto(type, Object.class);
                for (Field recursiveField : fields) {
                    if (recursiveField.getName().equals(name)) {
                        field = recursiveField;
                        break;
                    }
                }
            }
            if (field == null) {
                throw new ValidationException("no such field \'" + name + "\' for type " + type.getName());
            }
            return field;
        }
    }

    private <T> Field getEmbeddedField(Class<T> startingClass, String embeddedField) {
        String regex = "\\" + NitriteConfig.getFieldSeparator();
        String[] split = embeddedField.split(regex, 2);
        String key = split[0];
        String remaining = split.length == 2 ? split[1] : "";

        if (isNullOrEmpty(key)) {
            throw new ValidationException("invalid embedded field provided");
        }

        Field field;
        try {
            field = startingClass.getDeclaredField(key);
        } catch (NoSuchFieldException nsfe) {
            throw new ValidationException("no such field \'" + key + "\' for type " + startingClass.getName());
        }

        if (!isNullOrEmpty(remaining) || remaining.contains(NitriteConfig.getFieldSeparator())) {
            return getEmbeddedField(field.getType(), remaining);
        } else {
            return field;
        }
    }

    private <T> void populateIndex(NitriteMapper nitriteMapper, Class<T> type,
                                   List<Index> indexList, Set<Index> indexSet) {
        for (Index index : indexList) {
            String name = index.value();
            Field field = getField(type, name, true);
            if (field != null) {
                validateObjectIndexField(nitriteMapper, field.getType(), field.getName());
                indexSet.add(index);
            }
        }
    }

    private void validateObjectIndexField(NitriteMapper nitriteMapper, Class<?> fieldType, String field) {
        if (!Comparable.class.isAssignableFrom(fieldType) && !fieldType.isPrimitive()) {
            throw new IndexingException("cannot index on non comparable field " + field);
        }

        if (Iterable.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
            throw new IndexingException("indexing on arrays or collections for field " + field
                + " are not supported");
        }

        if (fieldType.isPrimitive()
            || fieldType == NitriteId.class
            || fieldType.isInterface()
            || Modifier.isAbstract(fieldType.getModifiers())) {
            // we will validate the solid class during insertion/update
            return;
        }

        Document document;
        try {
            document = skeletonDocument(nitriteMapper, fieldType);
        } catch (Throwable e) {
            throw new IndexingException("invalid type specified " + fieldType.getName() + " for indexing", e);
        }

        if (document == null || document.size() > 0) {
            throw new InvalidOperationException("compound index on field " + field + " is not supported");
        }
    }

    List<Field> getFieldsUpto(Class<?> startClass, Class<?> exclusiveParent) {
        notNull(startClass, "startClass cannot be null");
        List<Field> currentClassFields = new ArrayList<>(Arrays.asList(startClass.getDeclaredFields()));
        filterSynthetics(currentClassFields);
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null && !(parentClass.equals(exclusiveParent))) {
            List<Field> parentClassFields = getFieldsUpto(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    private static void filterSynthetics(List<Field> fields) {
        if (fields == null || fields.isEmpty()) return;
        Iterator<Field> iterator = fields.iterator();
        while (iterator.hasNext()) {
            Field f = iterator.next();
            if (f.isSynthetic()) iterator.remove();
        }
    }

    private <T extends Annotation> List<T> findAnnotations(Class<T> annotation, Class<?> type) {
        notNull(type, "type cannot be null");
        notNull(annotation, "annotationClass cannot be null");
        List<T> annotations = new ArrayList<>();

        T t = type.getAnnotation(annotation);
        if (t != null) annotations.add(t);

        Class[] interfaces = type.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            T ann = anInterface.getAnnotation(annotation);
            if (ann != null) annotations.add(ann);
        }

        Class<?> parentClass = type.getSuperclass();
        if (parentClass != null && !parentClass.equals(Object.class)) {
            List<T> list = findAnnotations(annotation, parentClass);
            annotations.addAll(list);
        }

        return annotations;
    }
}