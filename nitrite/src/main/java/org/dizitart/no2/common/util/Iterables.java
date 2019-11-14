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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A utility class for {@link Iterable}.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
@UtilityClass
public class Iterables {

    /**
     * Gets the first element of an {@link Iterable} or
     * `null` if it is empty.
     *
     * @param <T>      the type parameter
     * @param iterable the iterable
     * @return the first element or `null`.
     */
    public static <T> T firstOrNull(Iterable<T> iterable) {
        if (iterable == null) return null;

        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * Converts an {@link Iterable} into a {@link List}.
     *
     * @param <T>      the type parameter
     * @param iterable the iterable
     * @return the list containing all elements of the `iterable`.
     */
    public static <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T item : iterable) {
            list.add(item);
        }
        return list;
    }

    /**
     * Converts an {@link Iterable} of type `T` into an array of type `T`.
     *
     * @param <T>      the type parameter
     * @param iterable the iterable
     * @param type     the type
     * @return the array of type `T`.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Iterable<T> iterable, Class<T> type) {
        T[] dummy = (T[]) Array.newInstance(type, 0);
        if (iterable instanceof Collection) {
            return ((Collection<T>) iterable).toArray(dummy);
        } else {
            List<T> list = new ArrayList<>();
            for (T item : iterable) {
                list.add(item);
            }
            return list.toArray(dummy);
        }
    }

    @SuppressWarnings("unchecked")
    static Object[] toArray(Iterable iterable) {
        if (iterable instanceof Collection) {
            return ((Collection) iterable).toArray();
        } else {
            List list = new ArrayList();
            for (Object item : iterable) {
                list.add(item);
            }
            return list.toArray();
        }
    }
}
