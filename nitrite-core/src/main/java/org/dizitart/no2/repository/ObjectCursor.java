package org.dizitart.no2.repository;

import org.dizitart.no2.Document;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.ReadableStream;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.mapper.NitriteMapper;

import java.lang.reflect.Modifier;
import java.text.Collator;
import java.util.Iterator;

import static org.dizitart.no2.common.util.DocumentUtils.skeletonDocument;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee.
 */
class ObjectCursor<T> implements Cursor<T> {
    private DocumentCursor cursor;
    private NitriteMapper nitriteMapper;
    private Class<T> type;

    public ObjectCursor(NitriteMapper nitriteMapper, DocumentCursor cursor, Class<T> type) {
        this.nitriteMapper = nitriteMapper;
        this.cursor = cursor;
        this.type = type;
    }

    @Override
    public <P> ReadableStream<P> project(Class<P> projectionType) {
        notNull(projectionType, "projection cannot be null");
        Document dummyDoc = emptyDocument(nitriteMapper, projectionType);
        return new ProjectedObjectIterable<>(nitriteMapper, cursor.project(dummyDoc), projectionType);
    }

    @Override
    public <Foreign, Joined> ReadableStream<Joined> join(Cursor<Foreign> foreignCursor, Lookup lookup, Class<Joined> type) {
        ObjectCursor<Foreign> foreignObjectCursor = (ObjectCursor<Foreign>) foreignCursor;
        return new JoinedObjectIterable<>(nitriteMapper, cursor.join(foreignObjectCursor.cursor, lookup), type);
    }

    @Override
    public Cursor<T> sort(Field field, SortOrder sortOrder, Collator collator, NullOrder nullOrder) {
        return new ObjectCursor<>(nitriteMapper, cursor.sort(field, sortOrder, collator, nullOrder), type);
    }

    @Override
    public Cursor<T> limit(int offset, int size) {
        return new ObjectCursor<>(nitriteMapper, cursor.limit(offset, size), type);
    }

    @Override
    public Iterator<T> iterator() {
        return new ObjectCursorIterator(cursor.iterator());
    }

    private <D> Document emptyDocument(NitriteMapper nitriteMapper, Class<D> type) {
        if (type.isPrimitive()) {
            throw new ValidationException("cannot project to primitive type");
        } else if (type.isInterface()) {
            throw new ValidationException("cannot project to interface");
        } else if (type.isArray()) {
            throw new ValidationException("cannot project to array");
        } else if (Modifier.isAbstract(type.getModifiers())) {
            throw new ValidationException("cannot project to abstract type");
        }

        Document dummyDoc = skeletonDocument(nitriteMapper, type);
        if (dummyDoc == null) {
            throw new ValidationException("cannot project to empty type");
        } else {
            return dummyDoc;
        }
    }

    private class ObjectCursorIterator implements Iterator<T> {
        private Iterator<Document> documentIterator;

        ObjectCursorIterator(Iterator<Document> documentIterator) {
            this.documentIterator = documentIterator;
        }

        @Override
        public boolean hasNext() {
            return documentIterator.hasNext();
        }

        @Override
        public T next() {
            Document document = documentIterator.next();
            return nitriteMapper.asObject(document, type);
        }

        @Override
        public void remove() {
            throw new InvalidOperationException("remove on a cursor is not supported");
        }
    }
}
