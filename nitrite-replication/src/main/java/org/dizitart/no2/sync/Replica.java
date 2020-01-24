package org.dizitart.no2.sync;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.events.CollectionEventInfo;
import org.dizitart.no2.collection.events.CollectionEventListener;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class Replica extends WebSocketListener implements CollectionEventListener, AutoCloseable {
    private ReplicationConfig replicationConfig;
    private LocalOperation localOperation;
    private RemoteOperation remoteOperation;
    private WebSocket webSocket;
    private OkHttpClient client;
    private boolean connected = false;

    public static ReplicaBuilder builder() {
        return new ReplicaBuilder();
    }

    Replica(ReplicationConfig config) {
        this.replicationConfig = config;
        this.localOperation = new LocalOperation(replicationConfig);
        this.remoteOperation = new RemoteOperation(replicationConfig, getReplicaId());
    }

    public void connect() {
        try {
            ensureConnection();
            localOperation.sendConnect(webSocket);
            localOperation.sendLocalChanges(webSocket);

            replicationConfig.getCollection().subscribe(this);
        } catch (Exception e) {
            log.error("Error while connecting the replica", e);
            throw new ReplicationException("failed to open connection", e);
        }
    }

    public void disconnect() {
        try {
            localOperation.sendDisconnect(webSocket);
            close();
        } catch (Exception e) {
            log.error("Error while disconnecting the replica", e);
            throw new ReplicationException("failed to disconnect the replica", e);
        }
    }

    @Override
    public void onEvent(CollectionEventInfo<?> eventInfo) {
        Document document;
        switch(eventInfo.getEventType()) {
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
        connected = true;
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        connected = false;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        log.error("Connection interrupted due to error", t);
        super.onFailure(webSocket, t, response);
        connected = false;
        ensureConnection();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        System.out.println("Handling server message - " + text);
        remoteOperation.handleMessage(webSocket, text);
    }

    public String getReplicaId() {
        return localOperation.getReplicaId();
    }

    private OkHttpClient createClient() {
        return new OkHttpClient.Builder()
            .readTimeout(replicationConfig.getConnectTimeout().getTime(),
                replicationConfig.getConnectTimeout().getTimeUnit())
            .build();
    }

    private void ensureConnection() {
        if (!connected) {
            configure();
        }
    }

    @Override
    public void close() {
        webSocket.close(1000, null);
        client.dispatcher().executorService().shutdown();
        connected = false;
    }

    private void configure() {
        try {
            client = createClient();
            Request request = replicationConfig.getRequestBuilder().build();
            webSocket = client.newWebSocket(request, this);
        } catch (Exception e) {
            log.error("Error while establishing connection", e);
        }
    }
}
