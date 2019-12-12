package org.dizitart.no2;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.index.IndexType;
import org.junit.Before;
import org.junit.Test;

import static org.dizitart.no2.filters.FluentFilter.when;
import static org.dizitart.no2.index.IndexOptions.indexOptions;
import static org.junit.Assert.assertEquals;

/**
 * @author Anindya Chatterjee
 */
public class CollectionFieldIndexTest {
    private Nitrite db;

    @Before
    public void setUp() {
        db = NitriteBuilder.get().openOrCreate();
    }

    @Test
    public void testCollection() {
        Document doc1 = Document.createDocument("name", "Anindya")
            .put("color", new String[] {"red", "green", "blue"})
            .put("books", new Document[] {
                Document.createDocument("name", "Awesome Book")
                .put("tag", new String[] {"tag1", "tag2"}),
                Document.createDocument("name", "Another awesome book")
                .put("tag", new String[] {"tag3", "tag1"}),
                Document.createDocument("name", "No Tag")
            });

        Document doc2 = Document.createDocument("name", "Sandip")
            .put("color", new String[] {"purple", "yellow", "gray"})
            .put("books", new Document[] {
                Document.createDocument("name", "Awesome Book 2")
                    .put("tag", new String[] {"tag1", "tag2"}),
                Document.createDocument("name", "Another awesome book 2")
                    .put("tag", new String[] {"tag3", "tag1"}),
                Document.createDocument("name", "No Tag 2")
            });

        Document doc3 = Document.createDocument("name", "Subhra")
            .put("color", new String[] {"black", "sky", "violet"})
            .put("books", new Document[] {
                Document.createDocument("name", "Awesome Book 3")
                    .put("tag", new String[] {"tag1", "tag2"}),
                Document.createDocument("name", "Another awesome book")
                    .put("tag", new String[] {"tag3", "tag4"}),
                Document.createDocument("name", "No Tag")
            });

        NitriteCollection collection = db.getCollection("test");
        collection.createIndex("color", indexOptions(IndexType.Unique));
        collection.createIndex("books.tag", indexOptions(IndexType.NonUnique));
        collection.createIndex("books.name", indexOptions(IndexType.Fulltext));

        WriteResult writeResult = collection.insert(doc1, doc2, doc3);
        assertEquals(writeResult.getAffectedCount(), 3);

        DocumentCursor documents = collection.find(when("color").eq("red"));
        assertEquals(documents.firstOrNull(), doc1);
    }
}
