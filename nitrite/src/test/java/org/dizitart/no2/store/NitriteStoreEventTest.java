/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.store;

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.store.events.EventInfo;
import org.dizitart.no2.store.events.StoreEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.DbTestOperations.getRandomTempDbFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class NitriteStoreEventTest {
    private String dbFile;

    @Before
    public void before() {
        dbFile = getRandomTempDbFile();
    }

    @After
    public void cleanup() throws IOException {
        if (Files.exists(Paths.get(dbFile))) {
            Files.delete(Paths.get(dbFile));
        }
    }

    @Test
    public void testStoreEvents() {
        TestStoreEventListener listener = new TestStoreEventListener();
        assertFalse(listener.opened);
        assertFalse(listener.committed);
        assertFalse(listener.closing);
        assertFalse(listener.closed);

        Nitrite db = NitriteBuilder.get()
            .filePath(dbFile)
            .addStoreEventListener(listener)
            .openOrCreate();

        await().atMost(1, TimeUnit.SECONDS).until(() -> listener.opened);
        assertTrue(listener.opened);
        assertFalse(listener.committed);
        assertFalse(listener.closing);
        assertFalse(listener.closed);

        db.commit();

        await().atMost(1, TimeUnit.SECONDS).until(() -> listener.committed);
        assertTrue(listener.opened);
        assertTrue(listener.committed);
        assertFalse(listener.closing);
        assertFalse(listener.closed);

        db.close();

        await().atMost(1, TimeUnit.SECONDS).until(() -> listener.closed);
        assertTrue(listener.opened);
        assertTrue(listener.committed);
        assertTrue(listener.closing);
        assertTrue(listener.closed);

        db.getStore().unsubscribe(listener);
    }

    @Data
    private static class TestStoreEventListener implements StoreEventListener {
        private boolean opened;
        private boolean committed;
        private boolean closing;
        private boolean closed;

        @Override
        public void onEvent(EventInfo eventInfo) {
            switch (eventInfo.getEvent()) {
                case Opened:
                    opened = true;
                    break;
                case Commit:
                    committed = true;
                    break;
                case Closing:
                    closing = true;
                    break;
                case Closed:
                    closed = true;
                    break;
            }
        }
    }
}
