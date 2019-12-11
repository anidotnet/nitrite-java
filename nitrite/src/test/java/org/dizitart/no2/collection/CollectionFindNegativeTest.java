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

package org.dizitart.no2.collection;

import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.filters.FluentFilter.when;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee.
 */
public class CollectionFindNegativeTest extends BaseCollectionTest {
    @Test(expected = FilterException.class)
    public void testFindFilterInvalidIndex() {
        insert();
        collection.find(when("data.9").eq(4));
    }

    @Test(expected = ValidationException.class)
    public void testFindOptionsNegativeOffset() {
        insert();
        collection.find().limit(-1, 1);
    }

    @Test(expected = ValidationException.class)
    public void testFindOptionsNegativeSize() {
        insert();
        collection.find().limit(0, -1);
    }

    @Test(expected = ValidationException.class)
    public void testFindOptionsInvalidOffset() {
        insert();
        collection.find().limit(10, 1);
    }

    @Test(expected = InvalidOperationException.class)
    public void testFindInvalidSort() {
        insert();
        collection.find().sort("data", SortOrder.Descending);
    }

    @Test(expected = FilterException.class)
    public void testFindTextFilterNonIndexed() {
        insert();
        collection.find(when("body").text("Lorem"));
    }

    @Test(expected = FilterException.class)
    public void testFindWithRegexInvalidValue() {
        insert();
        DocumentCursor cursor = collection.find(when("birthDay").regex("hello"));
        assertEquals(cursor.size(), 1);
    }

    @Test(expected = ValidationException.class)
    public void testInvalidProjection() {
        insert();
        DocumentCursor cursor = collection.find(when("birthDay").lte(new Date())).
                sort("firstName", SortOrder.Ascending).limit(0, 3);

        Document projection = createDocument("firstName", null)
                .put("lastName", "ln2");

        cursor.project(projection);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToListAdd() {
        insert();
        DocumentCursor cursor = collection.find(when("lastName").eq("ln2"));
        List<Document> documents = cursor.toList();
        documents.add(createDocument());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToListRemove() {
        insert();
        DocumentCursor cursor = collection.find(when("lastName").eq("ln2"));
        List<Document> documents = cursor.toList();
        documents.clear();
    }
}
