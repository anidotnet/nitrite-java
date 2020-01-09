package org.dizitart.no2.sync.connection;

import com.neovisionaries.ws.client.WebSocket;
import org.dizitart.no2.sync.ReplicationException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anindya Chatterjee
 */
public class ConnectionPool {
    private static final ConnectionPool instance = new ConnectionPool();
    private final Map<String, Connection> pool;

    private ConnectionPool() {
        pool = new ConcurrentHashMap<>();
    }

    public static ConnectionPool create() {
        return instance;
    }

    public Connection getConnection(ConnectionConfig connectionConfig) {
        if (connectionConfig instanceof WebSocketConfig) {
            WebSocketConfig webSocketConfig = (WebSocketConfig) connectionConfig;
            return openWebsocketConnection(webSocketConfig);
        }

        throw new ReplicationException("failed to open a connection to remote server, wrong connection details provided");
    }

    private WebSocketConnection openWebsocketConnection(WebSocketConfig webSocketConfig) {
        if (pool.containsKey(webSocketConfig.getUrl())) {
            WebSocketConnection webSocketConnection = (WebSocketConnection) pool.get(webSocketConfig.getUrl());
            WebSocket webSocket = webSocketConnection.getWebSocket();
            if (webSocket.isOpen()) {
                return webSocketConnection;
            } else {
                pool.remove(webSocketConfig.getUrl());
                WebSocketConnection newConnection = new WebSocketConnection(webSocketConfig);
                pool.put(webSocketConfig.getUrl(), newConnection);
                return newConnection;
            }
        } else {
            WebSocketConnection newConnection = new WebSocketConnection(webSocketConfig);
            pool.put(webSocketConfig.getUrl(), newConnection);
            return newConnection;
        }
    }
}
