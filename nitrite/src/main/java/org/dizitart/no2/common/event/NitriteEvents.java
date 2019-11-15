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

package org.dizitart.no2.common.event;

import org.dizitart.no2.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.filters.Filter;

/**
 * Represents different types of collection modification
 * actions.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public enum NitriteEvents {
    /**
     * Insert action.
     *
     * @see NitriteCollection#insert(Object[])
     * @see org.dizitart.no2.collection.objects.ObjectRepository#insert(Object, Object[])
     * @see org.dizitart.no2.collection.objects.ObjectRepository#insert(Object[])
     */
    DataInsert,

    /**
     * Update action.
     *
     * @see NitriteCollection#update(Filter, Document)
     * @see NitriteCollection#update(Filter, Document, UpdateOptions)
     * @see org.dizitart.no2.collection.objects.ObjectRepository#update(Filter, Object)
     */
    DataUpdate,

    /**
     * Remove action.
     *
     * @see NitriteCollection#remove(Filter)
     * @see NitriteCollection#remove(Filter, RemoveOptions)
     * @see org.dizitart.no2.collection.objects.ObjectRepository#remove(Filter, RemoveOptions)
     */
    DataRemove,

    /**
     * Collection Drop action.
     *
     * @see NitriteCollection#drop()
     * @see ObjectRepository#drop()
     */
    CollectionDrop,

    /**
     * Collection Close action.
     *
     * @see NitriteCollection#close()
     * @see ObjectRepository#close()
     */
    CollectionClose,

    /**
     * Before Store Closing action.
     *
     * @see org.dizitart.no2.store.NitriteStore#close()
     */
    StoreClosing,

    /**
     * After Store Closing action.
     *
     * @see org.dizitart.no2.store.NitriteStore#close()
     */
    StoreClosed
}
