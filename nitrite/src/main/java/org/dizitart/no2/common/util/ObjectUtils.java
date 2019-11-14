/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.ObjectMappingException;
import org.dizitart.no2.exceptions.ValidationException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisSerializer;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.dizitart.no2.common.Constants.KEY_OBJ_SEPARATOR;
import static org.dizitart.no2.common.util.Iterables.toArray;
import static org.dizitart.no2.common.util.NumberUtils.compare;
import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * A utility class.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@UtilityClass
@Slf4j
public class ObjectUtils {
    private static Objenesis stdObjenesis = new ObjenesisStd(true);
    private static Objenesis serializerObjenesis = new ObjenesisSerializer(true);

    /**
     * Checks whether a collection name is a valid object repository name.
     *
     * @param collectionName the collection name
     * @return `true` if it is a valid object store name; `false` otherwise.
     */
    public static boolean isRepository(String collectionName) {
        try {
            if (isNullOrEmpty(collectionName)) return false;
            Class clazz = Class.forName(collectionName);
            return clazz != null;
        } catch (ClassNotFoundException e) {
            return isKeyedRepository(collectionName);
        }
    }

    /**
     * Checks whether a collection name is a valid keyed object repository name.
     *
     * @param collectionName the collection name
     * @return `true` if it is a valid object store name; `false` otherwise.
     */
    public static boolean isKeyedRepository(String collectionName) {
        try {
            if (isNullOrEmpty(collectionName)) return false;
            if (!collectionName.contains(KEY_OBJ_SEPARATOR)) return false;

            String[] split = collectionName.split("\\" + KEY_OBJ_SEPARATOR);
            if (split.length != 2) {
                return false;
            }
            String storeName = split[0];
            Class clazz = Class.forName(storeName);
            return clazz != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Gets the key name of a keyed-{@link org.dizitart.no2.collection.objects.ObjectRepository}
     *
     * @param collectionName name of the collection
     * @return the key
     * */
    public static String getKeyName(String collectionName) {
        if (collectionName.contains(KEY_OBJ_SEPARATOR)) {
            String[] split = collectionName.split("\\" + KEY_OBJ_SEPARATOR);
            return split[1];
        }
        throw new ValidationException(errorMessage(collectionName + " is not a valid keyed object repository",
                VE_INVALID_KEYED_OBJ_STORE_KEY));
    }

    /**
     * Gets the type name of a keyed-{@link org.dizitart.no2.collection.objects.ObjectRepository}
     *
     * @param collectionName name of the collection
     * @return the type name
     * */
    public static String getKeyedRepositoryType(String collectionName) {
        if (collectionName.contains(KEY_OBJ_SEPARATOR)) {
            String[] split = collectionName.split("\\" + KEY_OBJ_SEPARATOR);
            return split[0];
        }
        throw new ValidationException(errorMessage(collectionName + " is not a valid keyed object repository",
                VE_INVALID_KEYED_OBJ_STORE_TYPE));
    }

    /**
     * Computes equality of two objects.
     *
     * @param o1 the first object
     * @param o2 the other object
     * @return `true` if two objects are equal.
     */
    public static boolean deepEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        } else if (o1 == null || o2 == null) {
            return false;
        }

        if (o1 == o2) {
            // if reference equal send true
            return true;
        }

        if (o1 instanceof Number && o2 instanceof Number) {
            if (o1.getClass() != o2.getClass()) {
                return false;
            }
            // cast to Number and take care of boxing and compare
            return compare((Number) o1, (Number) o2) == 0;
        } else if (o1 instanceof Iterable && o2 instanceof Iterable)  {
            Object[] arr1 = toArray((Iterable) o1);
            Object[] arr2 = toArray((Iterable) o2);
            // convert iterable to array and recursively compare arrays
            return deepEquals(arr1, arr2);
        } else if (o1.getClass().isArray() && o2.getClass().isArray()) {
            // if both are object array iterate each element and recursively check
            // it respects cardinality of the elements in the array
            int length = Array.getLength(o1);

            if (length != Array.getLength(o2)) {
                return false;
            }

            for (int i = 0; i < length; i++) {
                Object item1 = Array.get(o1, i);
                Object item2 = Array.get(o2, i);

                if (!deepEquals(item1, item2)) {
                    // if one element is not equal return false
                    return false;
                }
            }
            // if all check passed it must be equal
            return true;
        } else if (o1 instanceof Map && o2 instanceof Map) {
            Map map1 = (Map) o1;
            Map map2 = (Map) o2;
            return deepEquals(toArray(map1.entrySet()), toArray(map2.entrySet()));
        } else {
            // generic check
            return o1.equals(o2);
        }

        // none of the type check passes so they are not of compatible type
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> type, boolean createSkeleton) {
        try {
            if (type.isPrimitive() || type.isArray() || type == String.class) {
                return defaultValue(type);
            }

            ObjectInstantiator instantiator = getInstantiatorOf(type);
            T item = (T) instantiator.newInstance();

            if (createSkeleton) {
                Field[] fields = type.getDeclaredFields();
                if (fields.length > 0) {
                    for (Field field : fields) {
                        // set value for non static fields
                        if (!Modifier.isStatic(field.getModifiers())) {
                            field.setAccessible(true);

                            // remove final modifier
                            Field modifiersField = Field.class.getDeclaredField("modifiers");
                            modifiersField.setAccessible(true);
                            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                            if (isSkeletonRequired(type, field.getType())) {
                                field.set(item, newInstance(field.getType(), true));
                            } else {
                                field.set(item, defaultValue(field.getType()));
                            }
                        }
                    }
                }
            }

            return item;
        } catch (Throwable e) {
            throw new ObjectMappingException(errorMessage("failed to instantiate type " + type.getName(),
                    OME_INSTANTIATE_FAILED), e);
        }
    }

    private static <T> ObjectInstantiator<T> getInstantiatorOf(Class<T> type) {
        if (Serializable.class.isAssignableFrom(type)) {
            return serializerObjenesis.getInstantiatorOf(type);
        } else {
            return stdObjenesis.getInstantiatorOf(type);
        }
    }

    private static <P, F> boolean isSkeletonRequired(Class<P> enclosingType, Class<F> fieldType) {
        String fieldTypePackage = getPackageName(fieldType);
        String enclosingTypePackage = getPackageName(enclosingType);

        return isCompatible(enclosingTypePackage, fieldTypePackage);
    }

    private static boolean isCompatible(String enclosingTypePackage, String fieldTypePackage) {
        if (enclosingTypePackage.contains(fieldTypePackage) && fieldTypePackage.contains(".")) return true;
        int lastDot = fieldTypePackage.lastIndexOf('.');
        if (lastDot == -1) return false;
        return isCompatible(enclosingTypePackage, fieldTypePackage.substring(0, lastDot));
    }

    private static <T> String getPackageName(Class<T> clazz) {
        String fqName = clazz.getName();
        int lastDot = fqName.lastIndexOf('.');
        if (lastDot == -1) return "";
        return fqName.substring(0, lastDot);
    }

    @SuppressWarnings("unchecked")
    private static <T> T defaultValue(Class<T> type) {
        if (type.isPrimitive()) {
            switch (type.getName()) {
                case "boolean":
                    return (T) Boolean.valueOf(false);
                case "byte":
                    return (T) Byte.valueOf((byte) 0);
                case "short":
                    return (T) Short.valueOf((short) 0);
                case "int":
                    return (T) Integer.valueOf(0);
                case "long":
                    return (T) Long.valueOf(0L);
                case "float":
                    return (T) Float.valueOf(0.0f);
                case "double":
                    return (T) Double.valueOf(0d);
                case "char":
                    return (T) Character.valueOf('\0');
            }
        }

        if (type.isArray()) {
            return null;
        }

        if (type == String.class) {
            return (T) "";
        }

        return null;
    }
}
