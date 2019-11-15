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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents affected item during collection modification.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangedItem<T> implements Serializable {
    /**
     * Specifies the changed item.
     *
     * @param item the item that changed.
     * @returns the item.
     * */
    private T item;

    /**
     * Specifies the change type.
     *
     * @param changeType the type of the change.
     * @returns the type of the change.
     * */
    private ChangeType changeType;

    /**
     * Specifies the unix timestamp of the change.
     *
     * @param changeTimestamp the unix timestamp of the change.
     * @returns the unix timestamp of the change.
     * */
    private long changeTimestamp;

    /**
     * Specifies the name of the thread where the change
     * has been originated.
     *
     * @param originatingThread name of originating thread.
     * @returns name of originating thread.
     * @since 4.0.0
     * */
    private String originatingThread;

    public ChangedItem(ChangeType changeType) {
        this.changeType = changeType;
    }
}
