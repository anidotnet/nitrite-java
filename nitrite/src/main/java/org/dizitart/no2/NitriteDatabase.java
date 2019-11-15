package org.dizitart.no2;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.event.DatabaseEvent;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.store.NitriteStore;

import java.nio.channels.NonWritableChannelException;
import java.util.Map;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.NIOE_CLOSED_NON_W_CHANNEL;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
class NitriteDatabase implements Nitrite {
    @Getter
    private final NitriteConfig nitriteConfig;

    @Getter
    private NitriteStore store;

    private NitriteEventBus eventBus;

    NitriteDatabase(NitriteConfig config) {
        this.nitriteConfig = config;
        this.initialize();
    }

    @Override
    public synchronized void close() {
        checkOpened();
        try {
            DatabaseEvent.Closing event = new DatabaseEvent.Closing(nitriteConfig);
            eventBus.post(event);

            store.beforeClose();
//            if (hasUnsavedChanges()) {
//                log.debug("Unsaved changes detected, committing the changes.");
//
//                // TODO: Trigger store closing initiated
//                // commit and compact in store implementation
//                commit();
//            }
//            if (context.isAutoCompactEnabled()) {
//                compact();
//            }

            closeCollections();
            store.close();

            DatabaseEvent.Closed closed = new DatabaseEvent.Closed(nitriteConfig);
            eventBus.post(closed);
            //                context.shutdown();
        } catch (NonWritableChannelException error) {
            if (!nitriteConfig.isReadOnly()) {
                throw new NitriteIOException(errorMessage("error while shutting down nitrite",
                    NIOE_CLOSED_NON_W_CHANNEL), error);
            }
        } finally {
            store = null;
            log.info("Nitrite database has been closed successfully.");
        }
    }

    private void initialize() {
        store = nitriteConfig.getNitriteStore();
        eventBus = NitriteEventBus.get();
    }

    private void closeCollections() {
        Set<String> collections = store.getCollectionNames();
        if (collections != null) {
            for (String name : collections) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && !collection.isClosed()) {
                    collection.close();
                }
            }
            collections.clear();
        }

        Map<String, Class<?>> repositories = store.getRepositoryRegistry();
        if (repositories != null) {
            for (String name : repositories.keySet()) {
                NitriteCollection collection = getCollection(name);
                if (collection != null && !collection.isClosed()) {
                    collection.close();
                }
            }
            repositories.clear();
        }
    }
}
