package org.dizitart.no2;

import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.WriteResult;
import org.dizitart.no2.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.collection.Field.of;
import static org.dizitart.no2.collection.filters.FluentFilter.when;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteTest {

    @Test
    public void test() {
        Nitrite db = NitriteBuilder.get().openOrCreate();
        db.addEventListener(eventInfo -> System.out.println("Event - " + eventInfo));
        NitriteCollection collection = db.getCollection("test-collection");
        Document document = Document.createDocument();
        document.put("first", 1).put("second", 2).put("third", 3).put("text", "a quick brown fox jump over the lazy dog");
        collection.createIndex(of("first"), IndexOptions.indexOptions(IndexType.Unique));
        collection.createIndex(of("text"), IndexOptions.indexOptions(IndexType.Fulltext));

        WriteResult res = collection.insert(document);
        System.out.println(res.getAffectedCount());

        DocumentCursor cursor = collection.find(when("first").eq(1).and(when("second").eq(2)));
        for (Document doc : cursor) {
            System.out.println(doc);
        }

        cursor = collection.find(when("text").text("qui*"));
        for (Document doc : cursor) {
            System.out.println(doc);
        }
    }
}
