package org.dizitart.no2.sync.connection;


import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocketListener;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.message.MessageHandler;

/**
 * @author Anindya Chatterjee
 */
@Data
@Slf4j
class WebSocketConnection implements Connection {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic ";
    private static final String BEARER = "Bearer ";

    private OkHttpClient client;
    private okhttp3.WebSocket webSocket;

    private WebSocketConfig config;
    private WebSocketListener listener;

    public WebSocketConnection(ConnectionConfig config, WebSocketListener listener) {
        this.config = (WebSocketConfig) config;
        this.listener = listener;
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
        client.dispatcher().executorService().shutdown();
    }

    private void init() {
        try {
            Request.Builder builder = new Request.Builder();
            switch (config.getAuthType()) {
                case Basic:
                    builder.addHeader(AUTHORIZATION, BASIC + config.getAuthToken());
                    break;
                case Jwt:
                    builder.addHeader(AUTHORIZATION, BEARER + config.getAuthToken());
                    break;
                case None:
                    break;
            }
            builder.url(config.getUrl());

            Request request = builder.build();
            webSocket = client.newWebSocket(request, listener);
        } catch (Exception e) {
            throw new ReplicationException("failed to open a websocket connection to " + config.getUrl(), e);
        }
    }
}
