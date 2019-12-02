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

import java.util.Iterator;

/**
 * @author Anindya Chatterjee
 */
public class TestUtil {
    /**
     * Determines whether the supplied `iterable` is sorted.
     *
     * @param <T>       the type parameter
     * @param iterable  the iterable
     * @param ascending a boolean value indicating whether to sort in ascending order
     * @return the boolean value indicating if `iterable` is sorted or not.
     */
    public static <T extends Comparable<? super T>> boolean isSorted(Iterable<T> iterable, boolean ascending) {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return true;
        }
        T t = iterator.next();
        while (iterator.hasNext()) {
            T t2 = iterator.next();
            if (ascending) {
                if (t.compareTo(t2) > 0) {
                    return false;
                }
            } else {
                if (t.compareTo(t2) < 0) {
                    return false;
                }
            }
            t = t2;
        }
        return true;
    }
}
