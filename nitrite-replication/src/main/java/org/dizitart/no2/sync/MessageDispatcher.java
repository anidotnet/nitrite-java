package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.sync.handlers.*;
import org.dizitart.no2.sync.message.*;

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

    public MessageDispatcher(Config config, ReplicationTemplate replicationTemplate) {
        this.replicationTemplate = replicationTemplate;
        this.transformer = new MessageTransformer(config.getObjectMapper());

        int core = Runtime.getRuntime().availableProcessors();
        this.executorService = ExecutorServiceManager.getThreadPool(core, SYNC_THREAD_NAME);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        DataGateMessage message = transformer.transform(text);
        MessageTemplate messageTemplate = replicationTemplate.getMessageTemplate();
        dispatch(messageTemplate, message);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        log.error("Communication failure", t);
        replicationTemplate.setDisconnected();
        replicationTemplate.getMessageTemplate().closeConnection(t.getMessage());
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        log.warn("Connection to server is closed due to {}", reason);
        replicationTemplate.stopReplication(reason);
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
                        replicationTemplate.stopReplication("Fatal replica error - " + error.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Error occurred while handling {} message", message.getMessageHeader().getMessageType(), e);
                    replicationTemplate.stopReplication("Fatal replica error - " + e.getMessage());
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
            case BatchChangeStart:
                break;
            case BatchChangeContinue:
                break;
            case BatchChangeEnd:
                break;
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
}
