package org.dizitart.no2.collection.operation;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.junit.Test;

import java.util.Iterator;

import static org.dizitart.no2.collection.Document.createDocument;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee.
 */
public class DocumentCursorTest {

    @Test
    public void testFindResult() {
        Nitrite db = NitriteBuilder.get().openOrCreate();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        DocumentCursor result = collection.find();
        assertTrue(result instanceof DocumentCursorImpl);
    }

    @Test(expected = InvalidOperationException.class)
    public void testIteratorRemove() {
        Nitrite db = NitriteBuilder.get().openOrCreate();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        DocumentCursor cursor = collection.find();
        Iterator<Document> iterator = cursor.iterator();
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    @Test
    public void testValidateProjection() {
        Nitrite db = NitriteBuilder.get().openOrCreate();
        NitriteCollection collection = db.getCollection("test");
        collection.insert(createDocument("first", "second"));

        Document projection = createDocument("first", createDocument("second", null));
        ReadableStream<Document> project = collection.find().project(projection);
        assertNotNull(project);
    }
}
