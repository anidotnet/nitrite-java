package org.dizitart.no2.sync.connection;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.message.MessageHandler;

/**
 * @author Anindya Chatterjee
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
class WebSocketConnection extends WebSocketAdapter implements Connection {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic ";
    private static final String BEARER = "Bearer ";

    private WebSocket webSocket;
    private WebSocketConfig config;
    private MessageHandler messageHandler;

    public WebSocketConnection(ConnectionConfig config, MessageHandler messageHandler) {
        this.config = (WebSocketConfig) config;
        this.messageHandler = messageHandler;
        init();
    }

    @Override
    public void open() {
        webSocket.addListener(this);
    }

    @Override
    public void sendMessage(String message) {
        ensureConnection();
        log.info("Sending message {}", message);
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
        messageHandler.handleMessage(text);
    }

    private void init() {
        try {
            WebSocketFactory factory = new WebSocketFactory();
            factory.setConnectionTimeout(config.getConnectTimeout());
            webSocket = factory.createSocket(config.getUrl())
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .addListener(this);

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

            webSocket.connect();
        } catch (Exception e) {
            throw new ReplicationException("failed to open a websocket connection to " + config.getUrl(), e);
        }
    }

    private void ensureConnection() {
        if (!webSocket.isOpen()) {
            try {
                webSocket = webSocket.recreate().connect();
            } catch (Exception e) {
                throw new ReplicationException("websocket connection failure", e);
            }
        }
    }
}
