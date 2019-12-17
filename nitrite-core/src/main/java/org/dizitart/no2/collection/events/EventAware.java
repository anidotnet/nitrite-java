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

package org.dizitart.no2.collection.events;

/**
 * Interface to be implemented by collections that wish to be aware
 * of any event.
 *
 * @since 4.0
 * @author Anindya Chatterjee.
 * @see EventType
 */
public interface EventAware {
    /**
     * Subscribes an {@link EventListener} instance to listen to any
     * collection events.
     *
     * @param listener the listener
     */
    void subscribe(EventListener listener);

    /**
     * Unsubscribes an {@link EventListener} instance.
     *
     * @param listener the listener.
     */
    void unsubscribe(EventListener listener);
}
