package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public final class Replica extends WebSocketListener implements CollectionEventListener, AutoCloseable {
    private ReplicationConfig config;
    private LocalReplica localReplica;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(ReplicationConfig config) {
        this.config = config;
        this.localReplica = new LocalReplica(config);
    }

    public void connect() {
        try {
            localReplica.connect();
            connected.compareAndSet(false, true);
            localOperation.sendConnect(webSocket);
            localOperation.sendLocalChanges(webSocket);

            config.getCollection().subscribe(this);
        } catch (Exception e) {
            log.error("Error while connecting the replica {}", getReplicaId(), e);
            throw new ReplicationException("failed to open connection", e);
        }
    }

    public void disconnect() {
        try {
            localOperation.sendDisconnect(webSocket);
            close();
        } catch (Exception e) {
            log.error("Error while disconnecting the replica {}", getReplicaId(), e);
            throw new ReplicationException("failed to disconnect the replica", e);
        }
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        Document document;
        switch (eventInfo.getEventType()) {
            case Insert:
            case Update:
                document = (Document) eventInfo.getItem();
                localOperation.handleInsertEvent(document, webSocket);
                break;
            case Remove:
                document = (Document) eventInfo.getItem();
                localOperation.handleRemoveEvent(document, webSocket);
                break;
            case IndexStart:
            case IndexEnd:
                break;
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        System.out.println("********** set to true at onOpen****************");
        connected.compareAndSet(false, true);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        log.error("Connection to {} is closed due to {}", getReplicaId(), reason);
        super.onClosed(webSocket, code, reason);
        System.out.println("********** set to false at onClosed****************");
        connected.compareAndSet(true, false);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        log.error("Connection to {} is interrupted due to error with response {}", getReplicaId(), response, t);
        super.onFailure(webSocket, t, response);
        System.out.println("********** set to false at onFailure****************");
        connected.compareAndSet(true, false);
        ensureConnection();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        log.debug("Message received at {} from server {}", getReplicaId(), text);
        super.onMessage(webSocket, text);
        try {
            remoteOperation.handleMessage(webSocket, text);
        } catch (ServerError serverError) {
            log.error("Closing connection from {} due to server error", getReplicaId(), serverError);
            if (serverError.isFatal()) {
                close();
            }
        } catch (ReplicationException re) {
            log.error("Error while processing message at {}", getReplicaId(), re);
        }
    }

    public String getReplicaId() {
        return localOperation.getReplicaId();
    }

    public Boolean isConnected() {
        return connected.get();
    }

    @Override
    public void close() {
        webSocket.close(1000, null);
        client.dispatcher().executorService().shutdown();
        System.out.println("********** set to false at close****************");
        connected.compareAndSet(true, false);
    }



    private void ensureConnection() {
        if (!connected.get()) {
            configure();

        }
    }
}
