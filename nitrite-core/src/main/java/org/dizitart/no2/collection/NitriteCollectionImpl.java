package org.dizitart.no2.collection;

import lombok.Getter;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.collection.index.IndexEntry;
import org.dizitart.no2.collection.index.IndexType;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.collection.operation.CollectionOperations;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;

import static org.dizitart.no2.common.util.DocumentUtils.createUniqueFilter;
import static org.dizitart.no2.common.util.ValidationUtils.containsNull;
import static org.dizitart.no2.common.util.ValidationUtils.notNull;
import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.*;

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
        notNull(documents, errorMessage("a null document cannot be inserted", VE_INSERT_NULL_DOCUMENT_ARRAY));
        containsNull(documents, errorMessage("a null document cannot be inserted",
            VE_INSERT_DOCUMENTS_CONTAINS_NULL));

        return collectionOperations.insert(documents);
    }

    @Override
    public WriteResult update(Document document, boolean insertIfAbsent) {
        checkOpened();
        notNull(document, errorMessage("a null document cannot be used for update", VE_UPDATE_NULL_DOCUMENT_OPTION));

        if (document.hasId()) {
            return update(createUniqueFilter(document), document, UpdateOptions.updateOptions(insertIfAbsent));
        } else {
            throw new NotIdentifiableException(UPDATE_FAILED_AS_NO_ID_FOUND);
        }
    }

    @Override
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        checkOpened();
        notNull(update, errorMessage("a null document cannot be used for update", VE_UPDATE_OPTIONS_NULL_DOCUMENT));
        notNull(updateOptions, errorMessage("updateOptions cannot be null", VE_UPDATE_NULL_UPDATE_OPTIONS));

        return collectionOperations.update(filter, update, updateOptions);
    }

    @Override
    public WriteResult remove(Document document) {
        checkOpened();
        notNull(document, errorMessage("a null document cannot be removed", VE_REMOVE_NULL_DOCUMENT));

        if (document.hasId()) {
            return remove(createUniqueFilter(document));
        } else {
            throw new NotIdentifiableException(REMOVE_FAILED_AS_NO_ID_FOUND);
        }
    }

    @Override
    public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
        checkOpened();
        notNull(removeOptions, errorMessage("removeOptions cannot be null", VE_REMOVE_NULL_DOCUMENT));

        return collectionOperations.remove(filter, removeOptions);
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
    public void createIndex(Field field, IndexOptions indexOptions) {
        checkOpened();
        notNull(field, errorMessage("field cannot be null", VE_CREATE_INDEX_NULL_FIELD));

        // by default async is false while creating index
        if (indexOptions == null) {
            collectionOperations.createIndex(field, IndexType.Unique, false);
        } else {
            collectionOperations.createIndex(field, indexOptions.getIndexType(),
                indexOptions.isAsync());
        }
    }

    @Override
    public void rebuildIndex(Field field, boolean isAsync) {
        checkOpened();
        notNull(field, errorMessage("field cannot be null", VE_REBUILD_INDEX_NULL_FIELD));

        IndexEntry indexEntry = collectionOperations.findIndex(field);
        if (indexEntry != null) {
            validateRebuildIndex(indexEntry);
            collectionOperations.rebuildIndex(indexEntry, isAsync);
        } else {
            throw new IndexingException(errorMessage(field + " is not indexed",
                IE_REBUILD_INDEX_FIELD_NOT_INDEXED));
        }
    }

    @Override
    public Collection<IndexEntry> listIndices() {
        checkOpened();
        return collectionOperations.listIndexes();
    }

    @Override
    public boolean hasIndex(Field field) {
        checkOpened();
        notNull(field, errorMessage("field cannot be null", VE_HAS_INDEX_NULL_FIELD));

        return collectionOperations.hasIndex(field);
    }

    @Override
    public boolean isIndexing(Field field) {
        checkOpened();
        notNull(field, errorMessage("field cannot be null", VE_IS_INDEXING_NULL_FIELD));
        return collectionOperations.isIndexing(field);
    }

    @Override
    public void dropIndex(Field field) {
        checkOpened();
        notNull(field, errorMessage("field cannot be null", VE_DROP_INDEX_NULL_FIELD));
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
        notNull(nitriteId, errorMessage("nitriteId cannot be null", VE_GET_BY_ID_NULL_ID));
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
        collectionOperations.close();
        this.nitriteMap = null;
        this.nitriteConfig = null;
        this.collectionOperations = null;
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
        notNull(listener, errorMessage("listener cannot be null", VE_LISTENER_NULL));

        eventBus.register(listener);
    }

    @Override
    public void deregister(ChangeListener listener) {
        checkOpened();
        notNull(listener, errorMessage("listener cannot be null", VE_LISTENER_DEREGISTER_NULL));

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
        notNull(attributes, errorMessage("attributes cannot be null", VE_ATTRIBUTE_NULL));
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
        this.collectionOperations = new CollectionOperations(nitriteMap, nitriteConfig, eventBus);
    }

    private void checkOpened() {
        if (isOpen()) return;

        if (isDropped) {
            throw new NitriteIOException(COLLECTION_IS_DROPPED);
        }

        if (nitriteStore == null || nitriteStore.isClosed()) {
            throw new NitriteIOException(STORE_IS_CLOSED);
        }
    }

    private void validateRebuildIndex(IndexEntry indexEntry) {
        notNull(indexEntry, errorMessage("index cannot be null", VE_NC_REBUILD_INDEX_NULL_INDEX));

        if (isIndexing(indexEntry.getField())) {
            throw new IndexingException(errorMessage("indexing on value " + indexEntry.getField() +
                " is currently running", IE_VALIDATE_REBUILD_INDEX_RUNNING));
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
