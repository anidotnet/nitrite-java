package org.dizitart.no2.collection.operation;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.DocumentCursor;
import org.dizitart.no2.collection.RemoveOptions;
import org.dizitart.no2.collection.UpdateOptions;
import org.dizitart.no2.collection.WriteResult;
import org.dizitart.no2.collection.events.ChangeListener;
import org.dizitart.no2.collection.events.ChangeType;
import org.dizitart.no2.collection.events.ChangedItem;
import org.dizitart.no2.collection.filters.Filter;
import org.dizitart.no2.common.event.EventBus;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.store.NitriteMap;

import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.common.Constants.*;
import static org.dizitart.no2.exceptions.ErrorCodes.UCE_CONSTRAINT_VIOLATED;
import static org.dizitart.no2.exceptions.ErrorMessage.OBJ_MULTI_UPDATE_WITH_JUST_ONCE;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
class WriteOperations {
    private final IndexOperations indexOperations;
    private final ReadOperations readOperations;
    private final EventBus<ChangedItem<Document>, ChangeListener> eventBus;
    private final NitriteMap<NitriteId, Document> nitriteMap;

    WriteOperations(IndexOperations indexOperations,
                    ReadOperations readOperations,
                    NitriteMap<NitriteId, Document> nitriteMap,
                    EventBus<ChangedItem<Document>, ChangeListener> eventBus) {
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
            Document already = nitriteMap.putIfAbsent(nitriteId, item);
            log.debug("Inserting document {} in {}", document, nitriteMap.getName());

            if (already != null) {
                // rollback changes
                nitriteMap.put(nitriteId, already);
                log.debug("Another document already exists with id {}", nitriteId);
                throw new UniqueConstraintException(errorMessage("id constraint violation, " +
                    "entry with same id already exists in " + nitriteMap.getName(), UCE_CONSTRAINT_VIOLATED));
            } else {
                try {
                    indexOperations.updateIndexEntry(item, nitriteId);
                } catch (UniqueConstraintException uce) {
                    log.error("Unique constraint violated for the document "
                        + document + " in " + nitriteMap.getName(), uce);
                    nitriteMap.remove(nitriteId);
                    throw uce;
                }
            }

            nitriteIdList.add(nitriteId);
            ChangedItem<Document> changedItem = new ChangedItem<>();
            changedItem.setItem(document);
            changedItem.setChangeTimestamp(document.getLastModifiedSinceEpoch());
            changedItem.setChangeType(ChangeType.Insert);
            alert(ChangeType.Insert, changedItem);
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
                throw new InvalidOperationException(OBJ_MULTI_UPDATE_WITH_JUST_ONCE);
            }

            update = update.clone();
            update.remove(DOC_ID);

            if (!REPLICATOR.contentEquals(update.getSource())) {
                update.remove(DOC_REVISION);
            }

            if (update.size() == 0) {
                alert(ChangeType.Update, null);
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

                    indexOperations.refreshIndexEntry(oldDocument, item, nitriteId);

                    ChangedItem<Document> changedItem = new ChangedItem<>();
                    changedItem.setItem(document);
                    changedItem.setChangeType(ChangeType.Update);
                    changedItem.setChangeTimestamp(document.getLastModifiedSinceEpoch());
                    alert(ChangeType.Update, changedItem);
                }
            }
        }

        log.debug("Returning write result {} for collection {}", writeResult, nitriteMap.getName());
        return writeResult;
    }

    WriteResult remove(Filter filter, RemoveOptions removeOptions) {
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
            filter, cursor.size(), removeOptions, nitriteMap.getName());

        for (Document document : cursor) {
            NitriteId nitriteId = document.getId();
            indexOperations.removeIndexEntry(document, nitriteId);

            Document removed = nitriteMap.remove(nitriteId);
            int rev = removed.getRevision();
            removed.put(DOC_REVISION, rev + 1);
            removed.put(DOC_MODIFIED, System.currentTimeMillis());

            log.debug("Document removed {} from {}", removed, nitriteMap.getName());
            result.addToList(nitriteId);

            ChangedItem<Document> changedItem = new ChangedItem<>();
            changedItem.setItem(removed);
            changedItem.setChangeType(ChangeType.Remove);
            changedItem.setChangeTimestamp(removed.getLastModifiedSinceEpoch());
            alert(ChangeType.Remove, changedItem);

            if (removeOptions.isJustOne()) {
                return result;
            }
        }

        log.debug("Returning write result {} for collection {}", result, nitriteMap.getName());
        return result;
    }

    private void alert(ChangeType action, ChangedItem<Document> changedItem) {
        log.debug("Notifying {} event for item {} from {}", action, changedItem, nitriteMap.getName());
        if (eventBus != null) {
            eventBus.post(changedItem);
        }
    }
}
