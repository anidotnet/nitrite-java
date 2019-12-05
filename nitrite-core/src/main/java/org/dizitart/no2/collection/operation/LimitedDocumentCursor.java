package org.dizitart.no2.collection.operation;

import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.common.LimitedIterator;
import org.dizitart.no2.common.ReadableStream;

import java.util.Iterator;

/**
 * @author Anindya Chatterjee.
 */
class LimitedDocumentCursor implements ReadableStream<NitriteId> {
    private ReadableStream<NitriteId> readableStream;
    private int offset;
    private int size;

    LimitedDocumentCursor(ReadableStream<NitriteId> readableStream, final int offset, final int size) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset parameter must not be negative.");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Max parameter must not be negative.");
        }

        this.readableStream = readableStream;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public Iterator<NitriteId> iterator() {
        return new LimitedIterator<>(readableStream.iterator(), offset, size);
    }
}
