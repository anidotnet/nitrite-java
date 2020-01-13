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
import org.dizitart.no2.sync.connection.Connection;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.message.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.collection.meta.Attributes.LAST_SYNCED;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class ReplicationOperation {
    private static final String TOMBSTONE = "tombstone";

    private ReplicationConfig replicationConfig;
    private NitriteCollection collection;
    private LastWriteWinMap crdt;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;

    public ReplicationOperation(ReplicationConfig replicationConfig) {
        this.replicationConfig = replicationConfig;
        this.collection = replicationConfig.getCollection();
        this.crdt = createReplicatedDataType();
        this.objectMapper = new ObjectMapper();
        this.executorService = ExecutorServiceManager.syncExecutor();
    }

    public void handleCollectionEvent(Connection connection, CollectionEventInfo<?> eventInfo) {
        Document document = (Document) eventInfo.getItem();
        switch (eventInfo.getEventType()) {
            case Insert:
                break;
            case Update:
                break;
            case Remove:
                break;
            case IndexStart:
                break;
            case IndexEnd:
                break;
        }
    }

    public void handleReplicationEvent(ReplicationEvent event) {

    }

    public void sendLocalChanges(Connection connection) {
        List<LastWriteWinState> changes = getLocalChanges();

        executorService.submit(() -> {
            String uuid = UUID.randomUUID().toString();
            String initMessage = createChangeStart(uuid, changes.size());
            connection.sendMessage(initMessage);

            for (LastWriteWinState change : changes) {
                String message = createChangeContinue(uuid, change);
                connection.sendMessage(message);

                try {
                    Thread.sleep(replicationConfig.getDebounce());
                } catch (InterruptedException e) {
                    log.error("thread interrupted", e);
                }
            }

            String endMessage = createChangeEnd(uuid);
            connection.sendMessage(endMessage);
        });
    }

    public List<LastWriteWinState> getLocalChanges() {
        try {
            Long lastSyncTime = getLastSyncTime();
            LastWriteWinState changes = crdt.getChanges(lastSyncTime);
            return changes.split(replicationConfig.getChunkSize());
        } catch (Exception e) {
            throw new ReplicationException("failed to create local changes message", e);
        }
    }

    private String createChangeStart(String uuid, Integer size) {
        try {
            LocalChangeStart message = new LocalChangeStart();
            message.setMessageInfo(createMessageInfo(MessageType.LocalChangeStart));
            message.setSize(size);
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

    private ReplicationStart createMessage(LastWriteWinState changes) {
        MessageInfo messageInfo = createMessageInfo(MessageType.ReplicationStart);
        ReplicationStart start = new ReplicationStart();
        start.setMessageInfo(messageInfo);
        start.setState(changes);
        return start;
    }

    private MessageInfo createMessageInfo(MessageType messageType) {
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setCollection(collection.getName());
        messageInfo.setMessageType(messageType);
        messageInfo.setServer(replicationConfig.getConnectionConfig().getUrl());
        messageInfo.setTimestamp(System.currentTimeMillis());
        messageInfo.setUserName(replicationConfig.getUserName());
        return messageInfo;
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
}
