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

package org.dizitart.no2.repository;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.common.event.ChangeListener;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.Index;
import org.dizitart.no2.meta.Attributes;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee
 */
public class RepositoryFactoryTest {
    @Test
    public void testRepositoryFactory() {
        RepositoryFactory factory = new RepositoryFactory();
        assertNotNull(factory);
    }

    @Test(expected = ValidationException.class)
    public void testNullType() {
        Nitrite db = Nitrite.builder().openOrCreate();
        RepositoryFactory.open(null, new DummyCollection(), db.getContext());
    }

    @Test(expected = ValidationException.class)
    public void testNullCollection() {
        Nitrite db = Nitrite.builder().openOrCreate();
        RepositoryFactory.open(DummyCollection.class, null, db.getContext());
    }

    @Test(expected = ValidationException.class)
    public void testNullContext() {
        RepositoryFactory.open(DummyCollection.class, new DummyCollection(), null);
    }

    private static class DummyCollection implements NitriteCollection {
        @Override
        public WriteResult insert(Document document, Document... documents) {
            return null;
        }

        @Override
        public WriteResult update(Filter filter, Document update) {
            return null;
        }

        @Override
        public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
            return null;
        }

        @Override
        public WriteResult remove(Filter filter) {
            return null;
        }

        @Override
        public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
            return null;
        }

        @Override
        public DocumentCursor find() {
            return null;
        }

        @Override
        public DocumentCursor find(Filter filter) {
            return null;
        }

        @Override
        public DocumentCursor find(FindOptions findOptions) {
            return null;
        }

        @Override
        public DocumentCursor find(Filter filter, FindOptions findOptions) {
            return null;
        }

        @Override
        public void createIndex(String field, IndexOptions indexOptions) {

        }

        @Override
        public void rebuildIndex(String field, boolean async) {

        }

        @Override
        public Collection<Index> listIndices() {
            return null;
        }

        @Override
        public boolean hasIndex(String field) {
            return false;
        }

        @Override
        public boolean isIndexing(String field) {
            return false;
        }

        @Override
        public void dropIndex(String field) {

        }

        @Override
        public void dropAllIndices() {

        }

        @Override
        public WriteResult insert(Document[] elements) {
            return null;
        }

        @Override
        public WriteResult update(Document element) {
            return null;
        }

        @Override
        public WriteResult update(Document element, boolean upsert) {
            return null;
        }

        @Override
        public WriteResult remove(Document element) {
            return null;
        }

        @Override
        public Document getById(NitriteId nitriteId) {
            return null;
        }

        @Override
        public void drop() {

        }

        @Override
        public boolean isDropped() {
            return false;
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void close() {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public void register(ChangeListener listener) {

        }

        @Override
        public void deregister(ChangeListener listener) {

        }

        @Override
        public Attributes getAttributes() {
            return null;
        }

        @Override
        public void setAttributes(Attributes attributes) {

        }
    }
}
