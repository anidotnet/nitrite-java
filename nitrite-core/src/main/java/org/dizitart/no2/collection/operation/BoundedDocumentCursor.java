package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.BoundedIterator;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.ValidationException;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Anindya Chatterjee.
 */
class BoundedDocumentCursor implements ReadableStream<NitriteId> {
    private final ReadableStream<NitriteId> readableStream;
    private final long offset;
    private final long limit;

    BoundedDocumentCursor(ReadableStream<NitriteId> readableStream, final long offset, final long limit) {
        if (offset < 0) {
            throw new ValidationException("offset parameter must not be negative");
        }
        if (limit < 0) {
            throw new ValidationException("limit parameter must not be negative");
        }

        this.readableStream = readableStream;
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public Iterator<NitriteId> iterator() {
        Iterator<NitriteId> iterator = readableStream == null ? Collections.emptyIterator()
            : readableStream.iterator();
        return new BoundedIterator<>(iterator, offset, limit);
    }
}
