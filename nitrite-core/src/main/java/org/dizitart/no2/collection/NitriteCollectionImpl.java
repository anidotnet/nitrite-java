package org.dizitart.no2.collection;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.collection.index.Index;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.collection.operation.CollectionOperation;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.store.NitriteStore;

import java.util.Collection;

/**
 * @author Anindya Chatterjee.
 */
class NitriteCollectionImpl implements NitriteCollection {
    private NitriteStore nitriteStore;
    private CollectionOperation collectionOperation;
    private EventBus<ChangedItem<Document>, ChangeListener> eventBus;
    private NitriteConfig nitriteConfig;
    private final String collectionName;
    private volatile boolean isDropped;

    NitriteCollectionImpl(String name, NitriteConfig nitriteConfig) {
        this.collectionName = name;
        this.nitriteConfig = nitriteConfig;
        initCollection();
    }

    @Override
    public WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        return null;
    }

    @Override
    public WriteResult remove(Filter filter, RemoveOptions removeOptions) {
        return null;
    }

    @Override
    public DocumentCursor find() {
        return null;
    }

    @Override
    public DocumentCursor find(Filter filter) {
        return null;
    }

    @Override
    public DocumentCursor find(FindOptions findOptions) {
        return null;
    }

    @Override
    public DocumentCursor find(Filter filter, FindOptions findOptions) {
        return null;
    }

    @Override
    public void createIndex(String field, IndexOptions indexOptions) {

    }

    @Override
    public void rebuildIndex(String field, boolean async) {

    }

    @Override
    public Collection<Index> listIndices() {
        return null;
    }

    @Override
    public boolean hasIndex(String field) {
        return false;
    }

    @Override
    public boolean isIndexing(String field) {
        return false;
    }

    @Override
    public void dropIndex(String field) {

    }

    @Override
    public void dropAllIndices() {

    }

    @Override
    public WriteResult insert(Document[] elements) {
        return null;
    }

    @Override
    public WriteResult update(Document element, boolean upsert) {
        return null;
    }

    @Override
    public WriteResult remove(Document element) {
        return null;
    }

    @Override
    public Document getById(NitriteId nitriteId) {
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
    public boolean isClosed() {
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

    private void initCollection() {
        nitriteStore = nitriteConfig.getNitriteStore();
        this.eventBus = new CollectionEventBus();
        this.collectionOperation = new CollectionOperation(nitriteStore, nitriteConfig, eventBus);
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
