package org.dizitart.no2.sync.connection;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Data;
import org.dizitart.no2.sync.ReplicationException;

import java.io.IOException;

/**
 * @author Anindya Chatterjee
 */
@Data
public class WebSocketConnection implements Connection {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic ";
    private static final String BEARER = "Bearer ";

    private WebSocket webSocket;
    private WebSocketConfig config;

    public WebSocketConnection(WebSocketConfig config) {
        this.config = config;
    }

    @Override
    public WebSocketConnection create() {
        try {
            WebSocketFactory factory = new WebSocketFactory();
            int connectTimeout = getTimeoutInMillis(config.getConnectTimeout());
            factory.setConnectionTimeout(connectTimeout);
            webSocket = factory.createSocket(config.getUrl());

            switch (config.getAuthType()) {
                case Basic:
                    webSocket.addHeader(AUTHORIZATION, BASIC + config.getAuthToken());
                    break;
                case Jwt:
                    webSocket.addHeader(AUTHORIZATION, BEARER + config.getAuthToken());
                    break;
                case None:
                    break;
            }
            return this;
        } catch (IOException ioe) {
            throw new ReplicationException("failed to open a websocket connection to " + config.getUrl(), ioe);
        }
    }

    private int getTimeoutInMillis(TimeSpan connectTimeout) {
        return Math.toIntExact(connectTimeout.getTimeUnit().toMillis(connectTimeout.getTime()));
    }
}
