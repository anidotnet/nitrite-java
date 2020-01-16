package org.dizitart.no2.collection.operation;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.events.CollectionEventListener;
import org.dizitart.no2.collection.events.EventType;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.store.NitriteMap;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.common.Constants.*;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class WriteOperations {
    private final IndexOperations indexOperations;
    private final ReadOperations readOperations;
    private final EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus;
    private final NitriteMap<NitriteId, Document> nitriteMap;

    WriteOperations(IndexOperations indexOperations,
                    ReadOperations readOperations,
                    NitriteMap<NitriteId, Document> nitriteMap,
                    EventBus<CollectionEventInfo<?>, CollectionEventListener> eventBus) {
        this.indexOperations = indexOperations;
        this.readOperations = readOperations;
        this.eventBus = eventBus;
        this.nitriteMap = nitriteMap;
    }

    WriteResult insert(Document... documents) {
        List<NitriteId> nitriteIdList = new ArrayList<>(documents.length);
        log.debug("Total {} document(s) to be inserted in {}", documents.length, nitriteMap.getName());

        for (Document document : documents) {
            NitriteId nitriteId = document.getId();

            if (!REPLICATOR.contentEquals(document.getSource())) {
                // if replicator is not inserting the document that means
                // it is being inserted by user, so update metadata
                document.remove(DOC_SOURCE);
                document.put(DOC_REVISION, 1);
                document.put(DOC_MODIFIED, System.currentTimeMillis());
            } else {
                // if replicator is inserting the document, remove the source
                // but keep the revision intact
                document.remove(DOC_SOURCE);
            }

            Document item = document.clone();
            log.debug("Inserting document {} in {}", item, nitriteMap.getName());
            Document already = nitriteMap.putIfAbsent(nitriteId, item);

            if (already != null) {
                log.warn("Another document {} already exists with same id {}", already, nitriteId);
                // rollback changes
                nitriteMap.put(nitriteId, already);
                throw new UniqueConstraintException("id constraint violation, " +
                    "entry with same id already exists in " + nitriteMap.getName());
            } else {
                try {
                    indexOperations.writeIndex(item, nitriteId);
                } catch (UniqueConstraintException uce) {
                    log.error("Unique constraint violated for the document "
                        + document + " in " + nitriteMap.getName(), uce);
                    nitriteMap.remove(nitriteId);
                    throw uce;
                }
            }

            nitriteIdList.add(nitriteId);
            CollectionEventInfo<Document> changedItem = new CollectionEventInfo<>();
            changedItem.setItem(document);
            changedItem.setTimestamp(document.getLastModifiedSinceEpoch());
            changedItem.setEventType(EventType.Insert);
            alert(EventType.Insert, changedItem);
        }

        WriteResultImpl result = new WriteResultImpl();
        result.setNitriteIdList(nitriteIdList);

        log.debug("Returning write result {} for collection {}", result, nitriteMap.getName());
        return result;
    }

    WriteResult update(Filter filter, Document update, UpdateOptions updateOptions) {
        DocumentCursor cursor;
        if (filter == null) {
            cursor = readOperations.find();
        } else {
            cursor = readOperations.find(filter);
        }

        WriteResultImpl writeResult = new WriteResultImpl();
        if (cursor == null || cursor.size() == 0) {
            log.debug("No document found to update by the filter {} in {}", filter, nitriteMap.getName());
            if (updateOptions.isInsertIfAbsent()) {
                return insert(update);
            } else {
                return writeResult;
            }
        } else {
            if (cursor.size() > 1 && updateOptions.isJustOnce()) {
                throw new InvalidOperationException("cannot update multiple items as justOnce is set to true");
            }

            update = update.clone();
            update.remove(DOC_ID);

            if (!REPLICATOR.contentEquals(update.getSource())) {
                update.remove(DOC_REVISION);
            }

            if (update.size() == 0) {
                alert(EventType.Update, null);
                return writeResult;
            }

            log.debug("Filter {} found total {} document(s) to update with options {} in {}",
                filter, cursor.size(), updateOptions, nitriteMap.getName());

            for(final Document document : cursor) {
                if (document != null) {
                    NitriteId nitriteId = document.getId();
                    Document oldDocument = document.clone();
                    log.debug("Document to update {} in {}", document, nitriteMap.getName());

                    if (!REPLICATOR.contentEquals(update.getSource())) {
                        update.remove(DOC_SOURCE);
                        document.putAll(update);
                        int rev = document.getRevision();
                        document.put(DOC_REVISION, rev + 1);
                        document.put(DOC_MODIFIED, System.currentTimeMillis());
                    } else {
                        update.remove(DOC_SOURCE);
                        document.putAll(update);
                    }

                    Document item = document.clone();
                    nitriteMap.put(nitriteId, item);
                    log.debug("Document {} updated in {}", document, nitriteMap.getName());

                    // if 'update' only contains id value, affected count = 0
                    if (update.size() > 0) {
                        writeResult.addToList(nitriteId);
                    }

                    indexOperations.updateIndex(oldDocument, item, nitriteId);

                    CollectionEventInfo<Document> changedItem = new CollectionEventInfo<>();
                    changedItem.setItem(document);
                    changedItem.setEventType(EventType.Update);
                    changedItem.setTimestamp(document.getLastModifiedSinceEpoch());
                    alert(EventType.Update, changedItem);
                }
            }
        }

        log.debug("Returning write result {} for collection {}", writeResult, nitriteMap.getName());
        return writeResult;
    }

    WriteResult remove(Filter filter, boolean justOne) {
        DocumentCursor cursor;
        if (filter == null) {
            cursor = readOperations.find();
        } else {
            cursor = readOperations.find(filter);
        }

        WriteResultImpl result = new WriteResultImpl();
        if (cursor == null) {
            log.debug("No document found to remove by the filter {} in {}", filter, nitriteMap.getName());
            return result;
        }

        log.debug("Filter {} found total {} document(s) to remove with options {} from {}",
            filter, cursor.size(), justOne, nitriteMap.getName());

        for (Document document : cursor) {
            NitriteId nitriteId = document.getId();
            indexOperations.removeIndex(document, nitriteId);

            Document removed = nitriteMap.remove(nitriteId);
            int rev = removed.getRevision();
            removed.put(DOC_REVISION, rev + 1);
            removed.put(DOC_MODIFIED, System.currentTimeMillis());

            log.debug("Document removed {} from {}", removed, nitriteMap.getName());
            result.addToList(nitriteId);

            CollectionEventInfo<Document> changedItem = new CollectionEventInfo<>();
            changedItem.setItem(removed);
            changedItem.setEventType(EventType.Remove);
            changedItem.setTimestamp(removed.getLastModifiedSinceEpoch());
            alert(EventType.Remove, changedItem);

            if (justOne) {
                return result;
            }
        }

        log.debug("Returning write result {} for collection {}", result, nitriteMap.getName());
        return result;
    }

    private void alert(EventType action, CollectionEventInfo<?> changedItem) {
        log.debug("Notifying {} event for item {} from {}", action, changedItem, nitriteMap.getName());
        if (eventBus != null) {
            eventBus.post(changedItem);
        }
    }
}
