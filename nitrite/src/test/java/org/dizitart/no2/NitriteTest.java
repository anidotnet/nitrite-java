package org.dizitart.no2;

import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.WriteResult;
import org.dizitart.no2.collection.index.IndexType;
import org.junit.Test;

import static org.dizitart.no2.collection.Field.of;
import static org.dizitart.no2.collection.filters.FluentFilter.when;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteTest {

    @Test
    public void test() {
        Nitrite db = Nitrite.openOrCreate();
        db.addEventListener(eventInfo -> System.out.println("Event - " + eventInfo));
        NitriteCollection collection = db.getCollection("test-collection");
        Document document = Document.createDocument();
        document.put("first", 1).put("second", 2).put("third", 3);
        collection.createIndex(of("first"), IndexOptions.indexOptions(IndexType.Unique));

        WriteResult res = collection.insert(document);
        System.out.println(res.getAffectedCount());

        DocumentCursor cursor = collection.find(when("first").eq(1).and(when("second").eq(3)));
        for (Document doc : cursor) {
            System.out.println(doc);
        }
    }
}
