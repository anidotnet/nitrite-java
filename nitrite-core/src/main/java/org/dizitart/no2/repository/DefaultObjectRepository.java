package org.dizitart.no2.repository;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Field;
import org.dizitart.no2.collection.IndexOptions;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.WriteResult;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.index.annotations.Index;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.Collection;
import java.util.Set;

import static org.dizitart.no2.collection.Field.of;
import static org.dizitart.no2.collection.IndexOptions.indexOptions;

/**
 * @author Anindya Chatterjee
 */
class DefaultObjectRepository<T> implements ObjectRepository<T> {
    private NitriteCollection collection;
    private Class<T> type;
    private NitriteMapper nitriteMapper;
    private RepositoryOperations operations;
    private java.lang.reflect.Field idField;

    DefaultObjectRepository(Class<T> type,
                                   NitriteCollection collection,
                                   NitriteConfig nitriteConfig) {
        this.type = type;
        this.collection = collection;
        init(nitriteConfig);
    }

    private void validateCollection() {
        if (collection == null) {
            throw new ValidationException("repository has not been initialized properly");
        }
    }

    @Override
    public WriteResult update(Filter filter, T update, boolean upsert) {
        return null;
    }

    @Override
    public WriteResult update(Filter filter, Document update, boolean justOnce) {
        return null;
    }

    @Override
    public WriteResult remove(Filter filter, boolean justOne) {
        return null;
    }

    @Override
    public Cursor<T> find() {
        return null;
    }

    @Override
    public Cursor<T> find(Filter filter) {
        return null;
    }

    @Override
    public Class<T> getType() {
        return null;
    }

    @Override
    public NitriteCollection getDocumentCollection() {
        return null;
    }

    @Override
    public void createIndex(Field field, IndexOptions indexOptions) {

    }

    @Override
    public void rebuildIndex(Field field, boolean isAsync) {

    }

    @Override
    public Collection<IndexEntry> listIndices() {
        return null;
    }

    @Override
    public boolean hasIndex(Field field) {
        return false;
    }

    @Override
    public boolean isIndexing(Field field) {
        return false;
    }

    @Override
    public void dropIndex(Field field) {

    }

    @Override
    public void dropAllIndices() {

    }

    @Override
    public WriteResult insert(T[] elements) {
        return null;
    }

    @Override
    public WriteResult update(T element, boolean insertIfAbsent) {
        return null;
    }

    @Override
    public WriteResult remove(T element) {
        return null;
    }

    @Override
    public T getById(NitriteId nitriteId) {
        return null;
    }

    @Override
    public void drop() {

    }

    @Override
    public boolean isDropped() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public void register(ChangeListener listener) {

    }

    @Override
    public void deregister(ChangeListener listener) {

    }

    @Override
    public Attributes getAttributes() {
        return null;
    }

    @Override
    public void setAttributes(Attributes attributes) {

    }

    private void init(NitriteConfig nitriteConfig) {
        nitriteMapper = nitriteConfig.nitriteMapper();
        operations = new RepositoryOperations();
        createIndexes();
    }

    private void createIndexes() {
        validateCollection();
        Set<Index> indexes = operations.extractIndices(nitriteMapper, type);
        for (Index idx : indexes) {
            Field field = of(idx.value());
            if (!collection.hasIndex(field)) {
                collection.createIndex(of(idx.value()), indexOptions(idx.type(), false));
            }
        }

        idField = operations.getIdField(nitriteMapper, type);
        if (idField != null) {
            Field field = of(idField.getName());
            if (!collection.hasIndex(field)) {
                collection.createIndex(field, indexOptions(IndexType.Unique));
            }
        }
    }
}
