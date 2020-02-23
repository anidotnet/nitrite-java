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
    private ReadableStream<NitriteId> readableStream;
    private long offset;
    private long size;

    BoundedDocumentCursor(ReadableStream<NitriteId> readableStream, final long offset, final long size) {
        if (offset < 0) {
            throw new ValidationException("offset parameter must not be negative.");
        }
        if (size < 0) {
            throw new ValidationException("size parameter must not be negative.");
        }

        this.readableStream = readableStream;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public Iterator<NitriteId> iterator() {
        Iterator<NitriteId> iterator = readableStream == null ? Collections.emptyIterator()
            : readableStream.iterator();
        return new BoundedIterator<>(iterator, offset, size);
    }
}
