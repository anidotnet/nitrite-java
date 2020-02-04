package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.sync.handlers.ConnectAckHandler;
import org.dizitart.no2.sync.handlers.DisconnectAckHandler;
import org.dizitart.no2.sync.handlers.MessageHandler;
import org.dizitart.no2.sync.handlers.ErrorHandler;
import org.dizitart.no2.sync.message.ConnectAck;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.message.DisconnectAck;
import org.dizitart.no2.sync.message.ErrorMessage;

import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.common.Constants.SYNC_THREAD_NAME;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MessageDispatcher extends WebSocketListener {
    private ReplicationConfig config;
    private LocalReplica replica;
    private MessageTransformer transformer;
    private ExecutorService executorService;

    public MessageDispatcher(ReplicationConfig config, LocalReplica replica) {
        this.config = config;
        this.replica = replica;
        this.transformer = new MessageTransformer(config.getObjectMapper());
        this.executorService = ExecutorServiceManager.getThreadPool(1, SYNC_THREAD_NAME);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        DataGateMessage message = transformer.transform(text);
        dispatch(webSocket, message);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        log.error("Communication failure", t);
        replica.getConnectedIndicator().compareAndSet(true, false);
        replica.getMessageTemplate().closeConnection(t.getMessage());
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        log.warn("Connection to server is closed due to {}", reason);
        replica.close(reason);
    }

    private <M extends DataGateMessage> void dispatch(WebSocket webSocket, M message) {
        MessageHandler<M> handler = findHandler(message);
        if (handler != null) {
            executorService.submit(() -> {
                try {
                    handler.handleMessage(webSocket, message);
                } catch (ReplicationException error) {
                    log.error("Error occurred while handling {} message", message.getMessageHeader().getMessageType(), error);
                    if (error.isFatal()) {
                        replica.close("Fatal replica error - " + error.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Error occurred while handling {} message", message.getMessageHeader().getMessageType(), e);
                    replica.close("Fatal replica error - " + e.getMessage());
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends DataGateMessage> MessageHandler<M> findHandler(DataGateMessage message) {
        // TODO: have to move logic in handler as much as possible
        // Create different getter field for ChangeManager, MessageTemplate, Crdt etc and use them in logic of handler
        if (message instanceof ConnectAck) {
            return (MessageHandler<M>) new ConnectAckHandler(replica);
        } else if (message instanceof ErrorMessage) {
            return (MessageHandler<M>) new ErrorHandler(replica);
        } else if (message instanceof DisconnectAck) {
            return (MessageHandler<M>) new DisconnectAckHandler(replica);
        }
        return null;
    }
}
