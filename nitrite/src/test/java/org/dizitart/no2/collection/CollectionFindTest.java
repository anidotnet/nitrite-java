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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.BaseCollectionTest;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.text.Collator;
import java.text.ParseException;
import java.util.*;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.Constants.DOC_REVISION;
import static org.dizitart.no2.common.util.TestUtil.isSorted;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.$;
import static org.dizitart.no2.filters.FluentFilter.when;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CollectionFindTest extends BaseCollectionTest {

    @Test
    public void testFindAll() {
        insert();

        DocumentCursor cursor = collection.find();
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testFindWithFilter() throws ParseException {
        insert();

        DocumentCursor cursor = collection.find(when("birthDay").gt(
            simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(when("birthDay").gte(
            simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(when("birthDay").lt(
            simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(when("birthDay").lte(
            simpleDateFormat.parse("2012-07-01T16:02:48.440Z")));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(when("birthDay").lte(
            new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(when("birthDay").lt(
            new Date()));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(when("birthDay").gt(
            new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(when("birthDay").gte(
            new Date()));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(
            when("birthDay").lte(new Date())
                .and(when("firstName").eq("fn1")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
            when("birthDay").lte(new Date())
                .or(when("firstName").eq("fn12")));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(
            when("birthDay").lte(new Date())
                .or(when("firstName").eq("fn12"))
                .and(when("lastName").eq("ln1")));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(
            when("birthDay").lte(new Date())
                .or(when("firstName").eq("fn12"))
                .and(when("lastName").eq("ln1")).not());
        assertEquals(cursor.size(), 2);


        cursor = collection.find(when("data.1").eq((byte) 4));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(when("data.1").lt(4));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(when("lastName").in("ln1", "ln2", "ln10"));
        assertEquals(cursor.size(), 3);

        cursor = collection.find(when("firstName").notIn("fn1", "fn2"));
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindWithLimit() {
        insert();

        DocumentCursor cursor = collection.find().limit(0, 1);
        assertEquals(cursor.size(), 1);

        cursor = collection.find().limit(1, 3);
        assertEquals(cursor.size(), 2);

        cursor = collection.find().limit(0, 30);
        assertEquals(cursor.size(), 3);

        cursor = collection.find().limit(2, 3);
        assertEquals(cursor.size(), 1);
    }

    @Test
    public void testFindSortAscending() {
        insert();

        DocumentCursor cursor = collection.find().sort("birthDay", SortOrder.Ascending);
        assertEquals(cursor.size(), 3);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, true));
    }

    @Test
    public void testFindSortDescending() {
        insert();

        DocumentCursor cursor = collection.find().sort("birthDay", SortOrder.Descending);
        assertEquals(cursor.size(), 3);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, false));
    }

    @Test
    public void testFindLimitAndSort() {
        insert();

        DocumentCursor cursor = collection.find().
            sort("birthDay", SortOrder.Descending).limit(1, 2);
        assertEquals(cursor.size(), 2);
        List<Date> dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, false));

        cursor = collection.find().
            sort("birthDay", SortOrder.Ascending).limit(1, 2);
        assertEquals(cursor.size(), 2);
        dateList = new ArrayList<>();
        for (Document document : cursor) {
            dateList.add(document.get("birthDay", Date.class));
        }
        assertTrue(isSorted(dateList, true));

        cursor = collection.find().
            sort("firstName", SortOrder.Ascending).limit(0, 30);
        assertEquals(cursor.size(), 3);
        List<String> nameList = new ArrayList<>();
        for (Document document : cursor) {
            nameList.add(document.get("firstName", String.class));
        }
        assertTrue(isSorted(nameList, true));
    }

    @Test
    public void testFindSortOnNonExistingField() {
        insert();
        DocumentCursor cursor = collection.find().sort("my-value", SortOrder.Descending);
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testFindInvalidField() {
        insert();
        DocumentCursor cursor = collection.find(when("myField").eq("myData"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindInvalidFieldWithInvalidAccessor() {
        insert();
        DocumentCursor cursor = collection.find(when("myField.0").eq("myData"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testFindLimitAndSortInvalidField() {
        insert();
        DocumentCursor cursor = collection.find().
            sort("birthDay2", SortOrder.Descending).limit(1, 2);
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testGetById() {
        collection.insert(doc1);
        NitriteId id = NitriteId.createId(1L);
        Document document = collection.getById(id);
        assertNull(document);

        document = collection.find().firstOrNull();

        assertEquals(document.get(DOC_ID), document.getId().getIdValue());
        assertEquals(document.get("firstName"), "fn1");
        assertEquals(document.get("lastName"), "ln1");
        assertArrayEquals((byte[]) document.get("data"), new byte[]{1, 2, 3});
        assertEquals(document.get("body"), "a quick brown fox jump over the lazy dog");
    }

    @Test
    public void testFindWithFilterAndOption() {
        insert();
        DocumentCursor cursor = collection.find(when("birthDay").lte(new Date())).
            sort("firstName", SortOrder.Ascending).limit(1, 2);
        assertEquals(cursor.size(), 2);
    }

    @Test
    public void testFindTextWithRegex() {
        insert();
        DocumentCursor cursor = collection.find(when("body").regex("hello"));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(when("body").regex("test"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(when("body").regex("^hello$"));
        assertEquals(cursor.size(), 0);

        cursor = collection.find(when("body").regex(".*"));
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testProject() {
        insert();
        DocumentCursor cursor = collection.find(when("birthDay").lte(new Date())).
            sort("firstName", SortOrder.Ascending).limit(0, 3);
        int iteration = 0;
        for (Document document : cursor) {
            switch (iteration) {
                case 0:
                    Assert.assertEquals(document, doc1);
                    break;
                case 1:
                    Assert.assertEquals(document, doc2);
                    break;
                case 2:
                    Assert.assertEquals(document, doc3);
                    break;
            }
            iteration++;
        }
        assertEquals(iteration, 3);
    }

    @Test
    public void testProjectWithCustomDocument() {
        insert();
        DocumentCursor cursor = collection.find(when("birthDay").lte(new Date())).
            sort("firstName", SortOrder.Ascending).limit(0, 3);

        Document projection = createDocument("firstName", null)
            .put("lastName", null);

        Iterable<Document> documents = cursor.project(projection);
        int iteration = 0;
        for (Document document : documents) {
            assertTrue(document.containsKey("firstName"));
            assertTrue(document.containsKey("lastName"));

            assertFalse(document.containsKey("_id"));
            assertFalse(document.containsKey("birthDay"));
            assertFalse(document.containsKey("data"));
            assertFalse(document.containsKey("body"));

            switch (iteration) {
                case 0:
                    assertEquals(document.get("firstName"), "fn1");
                    assertEquals(document.get("lastName"), "ln1");
                    break;
                case 1:
                    assertEquals(document.get("firstName"), "fn2");
                    assertEquals(document.get("lastName"), "ln2");
                    break;
                case 2:
                    assertEquals(document.get("firstName"), "fn3");
                    assertEquals(document.get("lastName"), "ln2");
                    break;
            }
            iteration++;
        }
        assertEquals(iteration, 3);
    }

    @Test
    public void testFindWithArrayEqual() {
        insert();
        DocumentCursor ids = collection.find(when("data").eq(new byte[]{3, 4, 3}));
        assertNotNull(ids);
        assertEquals(ids.size(), 1);
    }

    @Test
    public void testFindWithArrayEqualFailForWrongCardinality() {
        insert();
        DocumentCursor ids = collection.find(when("data").eq(new byte[]{4, 3, 3}));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);
    }

    @Test
    public void testFindWithIterableEqual() {
        insert();
        DocumentCursor ids = collection.find(when("list").eq(
            new ArrayList<String>() {{
                add("three");
                add("four");
                add("three");
            }}));
        assertNotNull(ids);
        assertEquals(ids.size(), 1);
    }

    @Test
    public void testFindWithIterableEqualFailForWrongCardinality() {
        insert();
        DocumentCursor ids = collection.find(when("list").eq(
            new ArrayList<String>() {{
                add("four");
                add("three");
                add("three");
            }}));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);
    }

    @Test
    public void testFindInArray() {
        insert();
        DocumentCursor ids = collection.find(when("data").elemMatch($.gte(2).and($.lt(5))));
        assertNotNull(ids);
        assertEquals(ids.size(), 3);

        ids = collection.find(when("data").elemMatch($.gt(2).or($.lte(5))));
        assertNotNull(ids);
        assertEquals(ids.size(), 3);

        ids = collection.find(when("data").elemMatch($.gt(1).and($.lt(4))));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);
    }

    @Test
    public void testFindInList() {
        insert();
        DocumentCursor ids = collection.find(when("list").elemMatch($.regex("three")));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);

        ids = collection.find(when("list").elemMatch($.regex("hello")));
        assertNotNull(ids);
        assertEquals(ids.size(), 0);

        ids = collection.find(when("list").elemMatch($.regex("hello").not()));
        assertNotNull(ids);
        assertEquals(ids.size(), 2);
    }

    @Test
    public void testElemMatchFilter() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue("{ productScores: [ { product: \"abc\", score: 10 }, " +
            "{ product: \"xyz\", score: 5 } ], strArray: [\"a\", \"b\"]}", Map.class);
        Document doc1 = createDocument(map);

        map = mapper.readValue("{ productScores: [ { product: \"abc\", score: 8 }, " +
            "{ product: \"xyz\", score: 7 } ], strArray: [\"d\", \"e\"] }", Map.class);
        Document doc2 = createDocument(map);

        map = mapper.readValue("{ productScores: [ { product: \"abc\", score: 7 }, " +
            "{ product: \"xyz\", score: 8 } ], strArray: [\"a\", \"f\"] }", Map.class);
        Document doc3 = createDocument(map);

        NitriteCollection prodCollection = db.getCollection("prodScore");
        prodCollection.insert(doc1, doc2, doc3);

        List<Document> documentList = prodCollection.find(when("productScores")
            .elemMatch(when("product").eq("xyz").and(when("score").gte(8)))).toList();

        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("score").lte(8).not())).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("product").eq("xyz").or(when("score").gte(8)))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("product").eq("xyz"))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("score").gte(10))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("score").gt(8))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("score").lt(7))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("score").lte(7))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("score").in(7, 8))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("score").notIn(7, 8))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(when("productScores")
            .elemMatch(when("product").regex("xyz"))).toList();
        assertEquals(documentList.size(), 3);

        documentList = prodCollection.find(when("strArray")
            .elemMatch($.eq("a"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(when("strArray")
            .elemMatch($.eq("a").or($.eq("f").or($.eq("b"))).not())).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(when("strArray")
            .elemMatch($.gt("e"))).toList();
        assertEquals(documentList.size(), 1);

        documentList = prodCollection.find(when("strArray")
            .elemMatch($.gte("e"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(when("strArray")
            .elemMatch($.lte("b"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(when("strArray")
            .elemMatch($.lt("a"))).toList();
        assertEquals(documentList.size(), 0);

        documentList = prodCollection.find(when("strArray")
            .elemMatch($.in("a", "f"))).toList();
        assertEquals(documentList.size(), 2);

        documentList = prodCollection.find(when("strArray")
            .elemMatch($.regex("a"))).toList();
        assertEquals(documentList.size(), 2);

    }

    @Test
    public void testNotEqualFilter() {
        Document document = createDocument("abc", "123");
        document.put("xyz", null);

        collection.insert(document);
        DocumentCursor cursor = collection.find(when("abc").eq("123"));
        assertEquals(cursor.size(), 1);
        assertEquals(cursor.toList().size(), 1);

        cursor = collection.find(when("xyz").eq(null));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(when("abc").eq(null).not());
        assertEquals(cursor.size(), 1);

        cursor = collection.find(when("abc").eq(null).not().and(when("xyz").eq(null)));
        assertEquals(cursor.size(), 1);

        cursor = collection.find(when("abc").eq(null).and(when("xyz").eq(null).not()));
        assertEquals(cursor.size(), 0);

        collection.remove(ALL);

        document = createDocument("field", "two");
        document.put(DOC_REVISION, 1482225343161L);

        collection.insert(document);
        Document projection = collection.find(
            when(DOC_REVISION).gte(1482225343160L)
                .and(when(DOC_REVISION).lte(1482225343162L)
                    .and(when(DOC_REVISION).eq(null).not())))
            .firstOrNull();

        assertNull(projection);
    }

    @Test
    public void testFilterAll() {
        DocumentCursor cursor = collection.find(ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 0);

        insert();
        cursor = collection.find(ALL);
        assertNotNull(cursor);
        assertEquals(cursor.size(), 3);
    }

    @Test
    public void testIssue72() {
        NitriteCollection coll = db.getCollection("test");
        coll.createIndex("id", IndexOptions.indexOptions(IndexType.Unique));
        coll.createIndex("group", IndexOptions.indexOptions(IndexType.NonUnique));

        coll.remove(ALL);

        Document doc = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        doc = createDocument().put("id", "test-2").put("group", "groupA").put("startTime", DateTime.now());
        assertEquals(1, coll.insert(doc).getAffectedCount());

        DocumentCursor cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending);
        assertEquals(2, cursor.size());
        assertNull(cursor.toList().get(1).get("startTime"));
        assertNotNull(cursor.toList().get(0).get("startTime"));

        cursor = coll.find(when("group").eq("groupA")).sort("startTime", SortOrder.Ascending);
        assertEquals(2, cursor.size());
        assertNull(cursor.toList().get(0).get("startTime"));
        assertNotNull(cursor.toList().get(1).get("startTime"));
    }

    @Test
    public void testIssue93() {
        NitriteCollection coll = db.getCollection("orderByOnNullableColumn2");

        coll.remove(ALL);

        Document doc = createDocument().put("id", "test-2").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        doc = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        DocumentCursor cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending);
        assertEquals(2, cursor.size());
    }

    @Test
    public void testNullOrderWithAllNull() {
        NitriteCollection coll = db.getCollection("test");

        coll.remove(ALL);

        Document doc = createDocument().put("id", "test-2").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        doc = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc).getAffectedCount());

        DocumentCursor cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending);
        assertEquals(2, cursor.size());

        DocumentCursor cursor2 = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.Default);
        assertEquals(2, cursor2.size());

        assertThat(cursor.toList(), is(cursor2.toList()));

        DocumentCursor cursor3 = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.First);
        assertEquals(2, cursor3.size());

        assertThat(cursor.toList(), is(cursor3.toList()));

        DocumentCursor cursor4 = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.Last);
        assertEquals(2, cursor4.size());

        assertThat(cursor.toList(), is(cursor4.toList()));

        DocumentCursor cursor5 = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.Last);
        assertEquals(2, cursor5.size());

        assertThat(cursor.toList(), is(cursor5.toList()));

        DocumentCursor cursor6 = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.First);
        assertEquals(2, cursor6.size());

        assertThat(cursor.toList(), is(cursor6.toList()));

        DocumentCursor cursor7 = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.Default);
        assertEquals(2, cursor7.size());

        assertThat(cursor.toList(), is(cursor7.toList()));
    }

    @Test
    public void testNullOrder() {
        NitriteCollection coll = db.getCollection("test");
        try {
            coll.createIndex("startTime", IndexOptions.indexOptions(IndexType.NonUnique));
        } catch (IndexingException e) {
            // ignore
        }

        coll.remove(ALL);

        Document doc1 = createDocument().put("id", "test-1").put("group", "groupA");
        assertEquals(1, coll.insert(doc1).getAffectedCount());

        Document doc2 = createDocument().put("id", "test-2").put("group", "groupA").put("startTime", DateTime.now());
        assertEquals(1, coll.insert(doc2).getAffectedCount());

        Document doc3 = createDocument().put("id", "test-3").put("group", "groupA").put("startTime", DateTime.now().plusMinutes(1));
        assertEquals(1, coll.insert(doc3).getAffectedCount());

        DocumentCursor cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc3, doc2, doc1), is(cursor.toList()));

        cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.First);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc1, doc3, doc2), is(cursor.toList()));

        cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.Default);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc3, doc2, doc1), is(cursor.toList()));

        cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Descending, NullOrder.Last);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc3, doc2, doc1), is(cursor.toList()));

        cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.First);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc1, doc2, doc3), is(cursor.toList()));

        cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.Default);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc1, doc2, doc3), is(cursor.toList()));

        cursor = coll.find(when("group").eq("groupA"))
            .sort("startTime", SortOrder.Ascending, NullOrder.Last);
        assertEquals(3, cursor.size());
        assertThat(Arrays.asList(doc2, doc3, doc1), is(cursor.toList()));
    }

    @Test
    public void testFindFilterInvalidAccessor() {
        insert();
        DocumentCursor cursor = collection.find(when("lastName.name").eq("ln2"));
        assertEquals(cursor.size(), 0);
    }

    @Test
    public void testIssue144() {
        Document doc1 = createDocument().put("id", "test-1").put("fruit", "Apple");
        Document doc2 = createDocument().put("id", "test-2").put("fruit", "Ôrange");
        Document doc3 = createDocument().put("id", "test-3").put("fruit", "Pineapple");

        NitriteCollection coll = db.getCollection("test");
        coll.insert(doc1, doc2, doc3);

        DocumentCursor cursor = coll.find().sort("fruit", SortOrder.Ascending,
            Collator.getInstance(Locale.FRANCE));
        assertEquals(cursor.toList().get(1).get("fruit"), "Ôrange");
    }

    @Test
    public void testIdSet() {
        insert();
        DocumentCursor cursor = collection.find(when("lastName").eq("ln2"));
        assertEquals(cursor.size(), 2);

        cursor = collection.find(when("lastName").eq("ln1"));
        assertEquals(cursor.size(), 1);

        Document byId = cursor.iterator().next();
        assertEquals(byId.get("lastName"), "ln1");
    }

    @Test
    public void testCollectionField() {
        Document document = createDocument("name", "John")
            .put("tags", new Document[]{
                createDocument("type", "example").put("other", "value"),
                createDocument("type", "another-example").put("other", "some-other-value")
            });

        NitriteCollection example = db.getCollection("example");
        example.insert(document);

        document = createDocument("name", "Jane")
            .put("tags", new Document[]{
                createDocument("type", "example2").put("other", "value2"),
                createDocument("type", "another-example2").put("other", "some-other-value2")
            });
        example.insert(document);

        DocumentCursor cursor = example.find(when("tags").elemMatch(when("type").eq("example")));
        for (Document doc : cursor) {
            assertNotNull(doc);
            assertEquals(doc.get("name"), "John");
        }
    }
}
