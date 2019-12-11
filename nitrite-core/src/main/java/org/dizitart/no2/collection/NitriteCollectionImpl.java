package org.dizitart.no2.collection;

import lombok.Getter;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.index.IndexEntry;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;

import static org.dizitart.no2.collection.UpdateOptions.updateOptions;
import static org.dizitart.no2.common.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;

/**
 * @author Anindya Chatterjee.
 */
class NitriteCollectionImpl implements NitriteCollection {
    private final String collectionName;
    private NitriteMap<NitriteId, Document> nitriteMap;
    private NitriteStore nitriteStore;
    private CollectionOperations collectionOperations;
    private EventBus<ChangedItem<Document>, ChangeListener> eventBus;
    private NitriteConfig nitriteConfig;

    @Getter
    private volatile boolean isDropped;

    NitriteCollectionImpl(String name, NitriteMap<NitriteId, Document> nitriteMap, NitriteConfig nitriteConfig) {
        this.collectionName = name;
        this.nitriteConfig = nitriteConfig;
        this.nitriteMap = nitriteMap;
        init();
    }

    @Override
    public WriteResult insert(Document[] documents) {
        checkOpened();
        notNull(documents, "a null document cannot be inserted");
        containsNull(documents, "a null document cannot be inserted");

        return collectionOperations.insert(documents);
    }

    @Override
    public WriteResult update(Document document, boolean insertIfAbsent) {
        checkOpened();
        notNull(document, "a null document cannot be used for update");

        if (insertIfAbsent) {
            return update(createUniqueFilter(document), document, updateOptions(true));
        } else {
            if (document.hasId()) {
                return update(createUniqueFilter(document), document, updateOptions(false));
            } else {
                throw new NotIdentifiableException("update operation failed as no id value found for the document");
            }
        }
    }

    @Override
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        checkOpened();
        notNull(update, "a null document cannot be used for update");
        notNull(updateOptions, "updateOptions cannot be null");

        return collectionOperations.update(filter, update, updateOptions);
    }

    @Override
    public WriteResult remove(Document document) {
        checkOpened();
        notNull(document, "a null document cannot be removed");

        if (document.hasId()) {
            return remove(createUniqueFilter(document));
        } else {
            throw new NotIdentifiableException("remove operation failed as no id value found for the document");
        }
    }

    @Override
    public WriteResult remove(Filter filter, boolean justOne) {
        checkOpened();
        return collectionOperations.remove(filter, justOne);
    }

    @Override
    public DocumentCursor find() {
        checkOpened();
        return collectionOperations.find();
    }

    @Override
    public DocumentCursor find(Filter filter) {
        checkOpened();
        return collectionOperations.find(filter);
    }

    @Override
    public void createIndex(String field, IndexOptions indexOptions) {
        checkOpened();
        notNull(field, "field cannot be null");

        // by default async is false while creating index
        if (indexOptions == null) {
            collectionOperations.createIndex(field, IndexType.Unique, false);
        } else {
            collectionOperations.createIndex(field, indexOptions.getIndexType(),
                indexOptions.isAsync());
        }
    }

    @Override
    public void rebuildIndex(String field, boolean isAsync) {
        checkOpened();
        notNull(field, "field cannot be null");

        IndexEntry indexEntry = collectionOperations.findIndex(field);
        if (indexEntry != null) {
            validateRebuildIndex(indexEntry);
            collectionOperations.rebuildIndex(indexEntry, isAsync);
        } else {
            throw new IndexingException(field + " is not indexed");
        }
    }

    @Override
    public Collection<IndexEntry> listIndices() {
        checkOpened();
        return collectionOperations.listIndexes();
    }

    @Override
    public boolean hasIndex(String field) {
        checkOpened();
        notNull(field, "field cannot be null");

        return collectionOperations.hasIndex(field);
    }

    @Override
    public boolean isIndexing(String field) {
        checkOpened();
        notNull(field, "field cannot be null");
        return collectionOperations.isIndexing(field);
    }

    @Override
    public void dropIndex(String field) {
        checkOpened();
        notNull(field, "field cannot be null");
        collectionOperations.dropIndex(field);
    }

    @Override
    public void dropAllIndices() {
        checkOpened();
        collectionOperations.dropAllIndices();
    }


    @Override
    public Document getById(NitriteId nitriteId) {
        checkOpened();
        notNull(nitriteId, "nitriteId cannot be null");
        return collectionOperations.getById(nitriteId);
    }

    @Override
    public void drop() {
        checkOpened();
        collectionOperations.dropCollection();
        isDropped = true;
        close();
    }

    @Override
    public boolean isOpen() {
        if (nitriteStore == null || nitriteStore.isClosed() || isDropped) {
            close();
            return false;
        }
        else return true;
    }

    @Override
    public void close() {
        this.nitriteMap = null;
        this.nitriteConfig = null;
        this.collectionOperations = null;
        this.nitriteStore = null;
        closeEventBus();
    }

    @Override
    public String getName() {
        return collectionName;
    }

    @Override
    public long size() {
        return collectionOperations.getSize();
    }

    @Override
    public void register(ChangeListener listener) {
        checkOpened();
        notNull(listener, "listener cannot be null");

        eventBus.register(listener);
    }

    @Override
    public void deregister(ChangeListener listener) {
        checkOpened();
        notNull(listener, "listener cannot be null");

        if (eventBus != null) {
            eventBus.deregister(listener);
        }
    }

    @Override
    public Attributes getAttributes() {
        checkOpened();
        return collectionOperations.getAttributes();
    }

    @Override
    public void setAttributes(Attributes attributes) {
        checkOpened();
        notNull(attributes, "attributes cannot be null");
        collectionOperations.setAttributes(attributes);
    }

    private void closeEventBus() {
        if (eventBus != null) {
            eventBus.close();
        }
        eventBus = null;
    }

    private void init() {
        nitriteStore = nitriteConfig.getNitriteStore();
        this.eventBus = new CollectionEventBus();
        this.collectionOperations = new CollectionOperations(collectionName, nitriteMap, nitriteConfig, eventBus);
    }

    private void checkOpened() {
        if (isOpen()) return;

        if (isDropped) {
            throw new NitriteIOException("collection has been dropped");
        }

        if (nitriteStore == null || nitriteStore.isClosed()) {
            throw new NitriteIOException("store is closed");
        }
    }

    private void validateRebuildIndex(IndexEntry indexEntry) {
        notNull(indexEntry, "index cannot be null");

        if (isIndexing(indexEntry.getField())) {
            throw new IndexingException("indexing on value " + indexEntry.getField() + " is currently running");
        }
    }

    private static class CollectionEventBus extends NitriteEventBus<ChangedItem<Document>, ChangeListener> {
        @Override
        public void post(ChangedItem<Document> changedItem) {
            for (final ChangeListener listener : getListeners()) {
                String threadName = Thread.currentThread().getName();
                changedItem.setOriginatingThread(threadName);

                getEventExecutor().submit(() -> listener.onChange(changedItem));
            }
        }
    }
}
