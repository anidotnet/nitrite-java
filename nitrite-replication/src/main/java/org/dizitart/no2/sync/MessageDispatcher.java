package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.event.ReplicationEvent;
import org.dizitart.no2.sync.event.ReplicationEventBus;
import org.dizitart.no2.sync.event.ReplicationEventType;
import org.dizitart.no2.sync.handlers.*;
import org.dizitart.no2.sync.message.DataGateMessage;

import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.common.Constants.SYNC_THREAD_NAME;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MessageDispatcher extends WebSocketListener {
    private ReplicationTemplate replicationTemplate;
    private MessageTransformer transformer;
    private ExecutorService executorService;
    private ReplicationEventBus eventBus;

    public MessageDispatcher(Config config, ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
        this.transformer = new MessageTransformer(config.getObjectMapper());

        int core = Runtime.getRuntime().availableProcessors();
        this.executorService = ThreadPoolManager.getThreadPool(core, SYNC_THREAD_NAME);
        this.eventBus = replicationTemplate.getEventBus();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            DataGateMessage message = transformer.transform(text);
            validateMessage(message);
            MessageTemplate messageTemplate = replicationTemplate.getMessageTemplate();
            dispatch(messageTemplate, message);
        } catch (Exception e) {
            log.error("Error while sending message", e);
            eventBus.post(new ReplicationEvent(ReplicationEventType.Error, e));
            replicationTemplate.stopReplication("Error - " + e.getMessage());
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        log.error("Communication failure", t);
        eventBus.post(new ReplicationEvent(ReplicationEventType.Error, t));
        replicationTemplate.stopReplication("Error - " + t.getMessage());
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        log.warn("Connection to server is closed due to {}", reason);
        replicationTemplate.stopReplication("Error - " + reason);
    }

    private <M extends DataGateMessage> void dispatch(MessageTemplate messageTemplate, M message) {
        MessageHandler<M> handler = findHandler(message);
        if (handler != null) {
            executorService.submit(() -> {
                try {
                    handler.handleMessage(messageTemplate, message);
                } catch (ReplicationException error) {
                    log.error("Error occurred while handling {} message", message.getMessageHeader().getMessageType(), error);
                    if (error.isFatal()) {
                        eventBus.post(new ReplicationEvent(ReplicationEventType.Error, error));
                        replicationTemplate.stopReplication("Error - " + error.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Error occurred while handling {} message", message.getMessageHeader().getMessageType(), e);
                    eventBus.post(new ReplicationEvent(ReplicationEventType.Error, e));
                    replicationTemplate.stopReplication("Error - " + e.getMessage());
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends DataGateMessage> MessageHandler<M> findHandler(DataGateMessage message) {
        switch (message.getMessageHeader().getMessageType()) {
            case Error:
                return (MessageHandler<M>) new ErrorHandler(replicationTemplate);
            case Connect:
                // impossible case, server will never initiate connection
                break;
            case ConnectAck:
                return (MessageHandler<M>) new ConnectAckHandler(replicationTemplate);
            case Disconnect:
                return (MessageHandler<M>) new DisconnectHandler(replicationTemplate);
            case DisconnectAck:
                return (MessageHandler<M>) new DisconnectAckHandler(replicationTemplate);
            case Checkpoint:
                if (replicationTemplate.shouldAcceptCheckpoint()) {
                    return (MessageHandler<M>) new CheckpointHandler(replicationTemplate);
                }
                break;
            case BatchChangeStart:
                return (MessageHandler<M>) new BatchChangeStartHandler(replicationTemplate);
            case BatchChangeContinue:
                return (MessageHandler<M>) new BatchChangeContinueHandler(replicationTemplate);
            case BatchChangeEnd:
                return (MessageHandler<M>) new BatchChangeEndHandler(replicationTemplate);
            case BatchAck:
                return (MessageHandler<M>) new BatchAckHandler(replicationTemplate);
            case BatchEndAck:
                return (MessageHandler<M>) new BatchEndAckHandler(replicationTemplate);
            case DataGateFeed:
                if (replicationTemplate.shouldExchangeFeed()) {
                    return (MessageHandler<M>) new DataGateFeedHandler(replicationTemplate);
                }
                break;
            case DataGateFeedAck:
                if (replicationTemplate.shouldExchangeFeed()) {
                    return (MessageHandler<M>) new DataGateFeedAckHandler(replicationTemplate);
                }
                break;
        }
        return null;
    }

    private void validateMessage(DataGateMessage message) {
        if (message == null) {
            throw new ReplicationException("a null message is received for "
                + replicationTemplate.getReplicaId(), true);
        } else if (message.getMessageHeader() == null) {
            throw new ReplicationException("a message without header is received for "
                + replicationTemplate.getReplicaId(), true);
        } else if (StringUtils.isNullOrEmpty(message.getMessageHeader().getCollection())) {
            throw new ReplicationException("a message without collection info is received for "
                + replicationTemplate.getReplicaId(), true);
        } else if (message.getMessageHeader().getMessageType() == null) {
            throw new ReplicationException("a message without any type is received for "
                + replicationTemplate.getReplicaId(), true);
        }
    }
}
