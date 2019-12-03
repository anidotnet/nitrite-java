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
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.repository.ObjectRepository;

/**
 * An interface when implemented makes an object be
 * able to listen to any changes in a {@link NitriteCollection}
 * or {@link ObjectRepository}.
 *
 * [[app-listing]]
 * [source,java]
 * .Example
 * --
 *
 *  // observe any change to the collection
 *  collection.register(new ChangeListener() {
 *
 *      @Override
 *      public void onChange(ChangedItem<Document> changedItem) {
 *          System.out.println("Action - " + changedItem.getChangeType());
 *
 *          System.out.println("Affected document - " + changedItem.getItem());
 *      }
 *  });
 *
 * --
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public interface ChangeListener {

    /**
     * Listener routine to be invoked for each change event.
     *
     * @param changedItem the change information
     */
    void onChange(ChangedItem<Document> changedItem);
}
