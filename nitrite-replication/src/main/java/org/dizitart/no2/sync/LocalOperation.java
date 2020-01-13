package org.dizitart.no2.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.sync.connection.ConnectionAware;
import org.dizitart.no2.sync.connection.ConnectionConfig;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.*;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.collection.meta.Attributes.LAST_SYNCED;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
public class LocalOperation implements ConnectionAware {
    private static final String TOMBSTONE = "tombstone";

    private ReplicationConfig config;
    private NitriteCollection collection;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;
    private LastWriteWinMap crdt;

    public LocalOperation(ReplicationConfig config) {
        this.config = config;
        this.collection = config.getCollection();
        this.crdt = createReplicatedDataType();
        this.objectMapper = config.getObjectMapper();
        this.executorService = ExecutorServiceManager.syncExecutor();
    }

    @Override
    public ConnectionConfig getConnectionConfig() {
        return config.getConnectionConfig();
    }

    public void handleCollectionEvent(CollectionEventInfo<?> eventInfo) {
        LastWriteWinState state = new LastWriteWinState();
        Document document;
        switch (eventInfo.getEventType()) {
            case Insert:
            case Update:
                document = (Document) eventInfo.getItem();
                state.setChanges(Collections.singleton(document));
                break;
            case Remove:
                document = (Document) eventInfo.getItem();
                NitriteId nitriteId = document.getId();
                Long deleteTime = document.getLastModifiedSinceEpoch();
                state.setTombstones(Collections.singletonMap(nitriteId, deleteTime));
                break;
            case IndexStart:
            case IndexEnd:
                break;
        }

        String message = createFeedMessage(state);
        getConnection().sendMessage(message);
        saveLastSyncTime();
    }

    private void saveLastSyncTime() {
        Attributes attributes = getAttributes();
        attributes.set(LAST_SYNCED, Long.toString(System.currentTimeMillis()));
        saveAttributes(attributes);
    }

    private String createFeedMessage(LastWriteWinState state) {
        try {
            DataGateFeed feed = new DataGateFeed();
            feed.setMessageInfo(createMessageInfo(MessageType.Feed));
            feed.setState(state);
            return objectMapper.writeValueAsString(feed);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create DataGateFeed message", e);
        }
    }

    public void sendLocalChanges() {
        try {
            Long lastSyncTime = getLastSyncTime();
            String uuid = UUID.randomUUID().toString();

            executorService.submit(() -> {
                int start = 0;
                boolean hasMore = true;

                String initMessage = createChangeStart(uuid);
                getConnection().sendMessage(initMessage);

                while (hasMore) {
                    LastWriteWinState state = crdt.getChangesSince(lastSyncTime, start, config.getChunkSize());
                    if (state.getChanges().size() == 0) {
                        hasMore = false;
                    }

                    if (hasMore) {
                        String message = createChangeContinue(uuid, state);
                        getConnection().sendMessage(message);

                        try {
                            Thread.sleep(config.getDebounce());
                        } catch (InterruptedException e) {
                            log.error("thread interrupted", e);
                        }

                        start = start + config.getChunkSize();
                    }
                }

                String endMessage = createChangeEnd(uuid);
                getConnection().sendMessage(endMessage);
            });
        } catch (Exception e) {
            throw new ReplicationException("failed to send local changes message", e);
        }
    }

    private LastWriteWinMap createReplicatedDataType() {
        Attributes attributes = getAttributes();
        String tombstoneName = getTombstoneName(attributes);
        saveAttributes(attributes);

        NitriteStore store = collection.getStore();
        NitriteMap<NitriteId, Long> tombstone = store.openMap(tombstoneName);
        return new LastWriteWinMap(collection, tombstone);
    }

    private String getTombstoneName(Attributes attributes) {
        String replica = attributes.get(TOMBSTONE);
        if (StringUtils.isNullOrEmpty(replica)) {
            replica = UUID.randomUUID().toString();
            attributes.set(TOMBSTONE, replica);
        }
        return replica;
    }

    private String createChangeStart(String uuid) {
        try {
            LocalChangeStart message = new LocalChangeStart();
            message.setMessageInfo(createMessageInfo(MessageType.LocalChangeStart));
            message.setUuid(uuid);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create LocalChangeStart message", e);
        }
    }

    private String createChangeContinue(String uuid, LastWriteWinState state) {
        try {
            LocalChangeContinue message = new LocalChangeContinue();
            message.setMessageInfo(createMessageInfo(MessageType.LocalChangeContinue));
            message.setState(state);
            message.setUuid(uuid);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create LocalChangeContinue message", e);
        }
    }

    private String createChangeEnd(String uuid) {
        try {
            LocalChangeEnd message = new LocalChangeEnd();
            message.setMessageInfo(createMessageInfo(MessageType.LocalChangeEnd));
            message.setUuid(uuid);
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new ReplicationException("failed to create LocalChangeEnd message", e);
        }
    }

    private MessageInfo createMessageInfo(MessageType messageType) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setCollection(collection.getName());
        messageInfo.setMessageType(messageType);
        messageInfo.setServer(config.getConnectionConfig().getUrl());
        messageInfo.setTimestamp(System.currentTimeMillis());
        messageInfo.setUserName(config.getUserName());
        return messageInfo;
    }

    private Long getLastSyncTime() {
        Attributes attributes = getAttributes();
        String syncTimeStr = attributes.get(LAST_SYNCED);
        if (StringUtils.isNullOrEmpty(syncTimeStr)) {
            return Long.MIN_VALUE;
        } else {
            try {
                return Long.parseLong(syncTimeStr);
            } catch (NumberFormatException nfe) {
                throw new ReplicationException("failed to retrieve last sync time for " + collection.getName(), nfe);
            }
        }
    }

    private Attributes getAttributes() {
        Attributes attributes = collection.getAttributes();
        if (attributes == null) {
            attributes = new Attributes();
            saveAttributes(attributes);
        }
        return attributes;
    }

    private void saveAttributes(Attributes attributes) {
        collection.setAttributes(attributes);
    }
}
