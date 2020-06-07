package org.dizitart.no2.rx;

import io.reactivex.internal.functions.ObjectHelper;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.common.Lookup;
import org.dizitart.no2.common.NullOrder;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.common.SortOrder;

import java.text.Collator;
import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
public final class FlowableDocumentCursor extends FlowableReadableStream<Document> {

    private final Callable<DocumentCursor> supplier;

    FlowableDocumentCursor(Callable<DocumentCursor> supplier) {
        super(supplier);
        this.supplier = supplier;
    }

    public FlowableDocumentCursor sort(String field) {
        Callable<DocumentCursor> sortSupplier = () -> {
            DocumentCursor cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.sort(field);
        };

        return new FlowableDocumentCursor(sortSupplier);
    }

    public FlowableDocumentCursor sort(String field, SortOrder sortOrder) {
        Callable<DocumentCursor> sortSupplier = () -> {
            DocumentCursor cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.sort(field, sortOrder);
        };

        return new FlowableDocumentCursor(sortSupplier);
    }

    public FlowableDocumentCursor sort(String field, SortOrder sortOrder, Collator collator) {
        Callable<DocumentCursor> sortSupplier = () -> {
            DocumentCursor cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.sort(field, sortOrder, collator);
        };

        return new FlowableDocumentCursor(sortSupplier);
    }

    public FlowableDocumentCursor sort(String field, SortOrder sortOrder, Collator collator, NullOrder nullOrder) {
        Callable<DocumentCursor> sortSupplier = () -> {
            DocumentCursor cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.sort(field, sortOrder, collator, nullOrder);
        };

        return new FlowableDocumentCursor(sortSupplier);
    }

    public FlowableDocumentCursor limit(int offset, int size) {
        Callable<DocumentCursor> sortSupplier = () -> {
            DocumentCursor cursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return cursor.skipLimit(offset, size);
        };

        return new FlowableDocumentCursor(sortSupplier);
    }

    public FlowableReadableStream<Document> project(Document projection) {
        Callable<ReadableStream<Document>> projectionSupplier = () -> {
            DocumentCursor documentCursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");
            return documentCursor.project(projection);
        };
        return FlowableReadableStream.create(projectionSupplier);
    }

    public FlowableReadableStream<Document> join(FlowableDocumentCursor foreignCursor, Lookup lookup) {
        Callable<ReadableStream<Document>> joinSupplier = () -> {
            DocumentCursor documentCursor = ObjectHelper.requireNonNull(supplier.call(),
                "The supplier supplied is null");

            DocumentCursor foreignDocumentCursor = ObjectHelper.requireNonNull(foreignCursor.supplier.call(),
                "The supplier supplied is null");

            return documentCursor.join(foreignDocumentCursor, lookup);
        };
        return FlowableReadableStream.create(joinSupplier);
    }
}
