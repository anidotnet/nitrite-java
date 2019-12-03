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
import org.dizitart.no2.collection.filters.NitriteFilter;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.mapper.NitriteMapper;

import java.util.Collection;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.Constants.DOC_ID;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee
 */
class DefaultObjectRepository<T> implements ObjectRepository<T> {
    private NitriteCollection collection;
    private Class<T> type;
    private NitriteMapper nitriteMapper;
    private RepositoryOperations operations;

    DefaultObjectRepository(Class<T> type,
                            NitriteCollection collection,
                            NitriteConfig nitriteConfig) {
        this.type = type;
        this.collection = collection;
        init(nitriteConfig);
    }

    @Override
    public void createIndex(Field field, IndexOptions indexOptions) {
        collection.createIndex(field, indexOptions);
    }

    @Override
    public void rebuildIndex(Field field, boolean isAsync) {
        collection.rebuildIndex(field, isAsync);
    }

    @Override
    public Collection<IndexEntry> listIndices() {
        return collection.listIndices();
    }

    @Override
    public boolean hasIndex(Field field) {
        return collection.hasIndex(field);
    }

    @Override
    public boolean isIndexing(Field field) {
        return collection.isIndexing(field);
    }

    @Override
    public void dropIndex(Field field) {
        collection.dropIndex(field);
    }

    @Override
    public void dropAllIndices() {
        collection.dropAllIndices();
    }

    @Override
    public WriteResult insert(T[] elements) {
        notNull(elements, "a null object cannot be inserted");
        containsNull(elements, "a null object cannot be inserted");
        return collection.insert(operations.toDocuments(elements));
    }

    @Override
    public WriteResult update(T element, boolean insertIfAbsent) {
        notNull(element, "a null object cannot be used for update");
        return update(operations.createUniqueFilter(element), element, insertIfAbsent);
    }

    @Override
    public WriteResult update(Filter filter, T update, boolean insertIfAbsent) {
        notNull(update, "a null object cannot be used for update");
        Document updateDocument = operations.toDocument(update, insertIfAbsent);
        operations.removeNitriteId(updateDocument);
        return collection.update(asObjectFilter(filter), updateDocument, updateOptions(insertIfAbsent, true));
    }

    @Override
    public WriteResult update(Filter filter, Document update, boolean justOnce) {
        notNull(update, "a null document cannot be used for update");
        operations.removeNitriteId(update);
        operations.serializeFields(update);
        return collection.update(asObjectFilter(filter), update, updateOptions(false, justOnce));
    }

    @Override
    public WriteResult remove(T element) {
        notNull(element, "a null object cannot be removed");
        return remove(operations.createUniqueFilter(element));
    }

    @Override
    public WriteResult remove(Filter filter, boolean justOne) {
        return collection.remove(asObjectFilter(filter), justOne);
    }

    @Override
    public Cursor<T> find() {
        return new ObjectCursor<>(nitriteMapper, collection.find(), type);
    }

    @Override
    public Cursor<T> find(Filter filter) {
        return new ObjectCursor<>(nitriteMapper, collection.find(asObjectFilter(filter)), type);
    }

    @Override
    public T getById(NitriteId nitriteId) {
        Document document = collection.getById(nitriteId);
        if (document != null) {
            Document item = document.clone();
            item.remove(DOC_ID);
            return nitriteMapper.asObject(item, type);
        }
        return null;
    }

    @Override
    public void drop() {
        collection.drop();
    }

    @Override
    public boolean isDropped() {
        return collection.isDropped();
    }

    @Override
    public boolean isOpen() {
        return collection.isOpen();
    }

    @Override
    public void close() {
        collection.close();
    }

    @Override
    public long size() {
        return collection.size();
    }

    @Override
    public void register(ChangeListener listener) {
        collection.register(listener);
    }

    @Override
    public void deregister(ChangeListener listener) {
        collection.deregister(listener);
    }

    @Override
    public Attributes getAttributes() {
        return collection.getAttributes();
    }

    @Override
    public void setAttributes(Attributes attributes) {
        collection.setAttributes(attributes);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public NitriteCollection getDocumentCollection() {
        return collection;
    }

    private void init(NitriteConfig nitriteConfig) {
        nitriteMapper = nitriteConfig.nitriteMapper();
        operations = new RepositoryOperations(type, nitriteMapper, collection);
        operations.createIndexes();
    }

    private Filter asObjectFilter(Filter filter) {
        if (filter instanceof NitriteFilter) {
            NitriteFilter nitriteFilter = (NitriteFilter) filter;
            nitriteFilter.setObjectFilter(true);
            return nitriteFilter;
        }
        return filter;
    }
}
