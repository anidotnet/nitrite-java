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
 *      public void onEvent(EventInfo<Document> eventInfo) {
 *          System.out.println("Action - " + eventInfo.getEventType());
 *
 *          System.out.println("Affected document - " + eventInfo.getItem());
 *      }
 *  });
 *
 * --
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public interface EventListener {

    /**
     * Listener routine to be invoked for each change event.
     *
     * @param eventInfo the change information
     */
    void onEvent(EventInfo<Document> eventInfo);
}
