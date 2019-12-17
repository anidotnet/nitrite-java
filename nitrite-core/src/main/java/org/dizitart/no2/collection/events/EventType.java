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

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;

/**
 * Represents different types of collection modification
 * actions.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public enum EventType {
    /**
     * Insert action.
     *
     * @see NitriteCollection#insert(Object[])
     * @see ObjectRepository#insert(Object, Object[])
     * @see ObjectRepository#insert(Object[])
     */
    Insert,

    /**
     * Update action.
     *
     * @see NitriteCollection#update(Filter, Document)
     * @see NitriteCollection#update(Filter, Document, UpdateOptions)
     * @see ObjectRepository#update(Filter, Object)
     */
    Update,

    /**
     * Remove action.
     *
     * @see NitriteCollection#remove(Filter)
     * @see NitriteCollection#remove(Filter, RemoveOptions)
     * @see ObjectRepository#remove(Filter, RemoveOptions)
     */
    Remove,

    IndexStart,

    IndexEnd
}
