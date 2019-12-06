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

package org.dizitart.no2.common.event;

import lombok.Getter;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangeType;
import org.dizitart.no2.collection.events.ChangedItem;

/**
 * @author Anindya Chatterjee.
 */
@Getter
class SampleListener implements ChangeListener {
    private ChangeType action;
    private Document item;

    @Override
    public void onChange(ChangedItem<Document> changeInfo) {
        this.action = changeInfo.getChangeType();
        this.item = changeInfo.getItem();
    }
}