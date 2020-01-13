package org.dizitart.no2.sync.connection;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.event.ReplicationEventBus;

import java.io.IOException;

/**
 * @author Anindya Chatterjee
 */
@Data
@EqualsAndHashCode(callSuper = true)
class WebSocketConnection extends WebSocketAdapter implements Connection {
    private static final ReplicationEventBus eventBus = ReplicationEventBus.getInstance();

    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic ";
    private static final String BEARER = "Bearer ";

    private WebSocket webSocket;
    private WebSocketConfig config;

    public WebSocketConnection(WebSocketConfig config) {
        this.config = config;
    }

    @Override
    public void open() {
        try {
            WebSocketFactory factory = new WebSocketFactory();
            factory.setConnectionTimeout(config.getConnectTimeout());
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
        } catch (IOException ioe) {
            throw new ReplicationException("failed to open a websocket connection to " + config.getUrl(), ioe);
        }
    }

    @Override
    public void sendAndReceive() {
        webSocket.addListener(this);
    }

    @Override
    public void sendMessage(String message) {
        webSocket.sendText(message);
    }

    @Override
    public void close() {
        webSocket.flush();
        webSocket.clearListeners();
        webSocket.disconnect();
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) {
        eventBus.handleMessage(text);
    }


}
