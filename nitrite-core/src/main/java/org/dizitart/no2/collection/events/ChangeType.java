/*
 *  Copyright 2017-2019 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.collection.events;

import org.dizitart.no2.Document;

/**
 * Represents different types of collection modification
 * actions.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public enum ChangeType {
    /**
     * Insert action.
     *
     * @see org.dizitart.no2.collection.NitriteCollection#insert(Object[])
     * @see org.dizitart.no2.collection.objects.ObjectRepository#insert(Object, Object[])
     * @see org.dizitart.no2.collection.objects.ObjectRepository#insert(Object[])
     */
    Insert,

    /**
     * Update action.
     *
     * @see org.dizitart.no2.collection.NitriteCollection#update(Filter, Document)
     * @see org.dizitart.no2.collection.NitriteCollection#update(Filter, Document, UpdateOptions)
     * @see org.dizitart.no2.collection.objects.ObjectRepository#update(Filter, Object)
     */
    Update,

    /**
     * Remove action.
     *
     * @see org.dizitart.no2.collection.NitriteCollection#remove(Filter)
     * @see org.dizitart.no2.collection.NitriteCollection#remove(Filter, RemoveOptions)
     * @see org.dizitart.no2.collection.objects.ObjectRepository#remove(Filter, RemoveOptions)
     */
    Remove,
}
