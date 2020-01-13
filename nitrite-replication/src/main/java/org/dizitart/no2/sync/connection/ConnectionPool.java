package org.dizitart.no2.sync.connection;

import com.neovisionaries.ws.client.WebSocket;
import org.dizitart.no2.sync.ReplicationConfig;
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

    public static ConnectionPool getInstance() {
        return instance;
    }

    public Connection getConnection(ReplicationConfig config) {
        ConnectionConfig connectionConfig = config.getConnectionConfig();

        if (connectionConfig instanceof WebSocketConfig) {
            return openWebsocketConnection(config);
        }

        throw new ReplicationException("failed to get a connection to remote server, wrong connection details provided");
    }

    private WebSocketConnection openWebsocketConnection(ReplicationConfig config) {
        ConnectionConfig connectionConfig = config.getConnectionConfig();
        WebSocketConfig webSocketConfig = (WebSocketConfig) connectionConfig;

        if (pool.containsKey(webSocketConfig.getUrl())) {
            WebSocketConnection webSocketConnection = (WebSocketConnection) pool.get(webSocketConfig.getUrl());
            WebSocket webSocket = webSocketConnection.getWebSocket();
            if (webSocket.isOpen()) {
                return webSocketConnection;
            } else {
                pool.remove(webSocketConfig.getUrl());
                WebSocketConnection newConnection = new WebSocketConnection(config);
                pool.put(webSocketConfig.getUrl(), newConnection);
                newConnection.open();
                return newConnection;
            }
        } else {
            WebSocketConnection newConnection = new WebSocketConnection(config);
            pool.put(webSocketConfig.getUrl(), newConnection);
            newConnection.open();
            return newConnection;
        }
    }
}
