package org.dizitart.no2.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import org.dizitart.no2.sync.message.DataGateMessage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.concurrent.TimeUnit;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MessageTemplate {
    private ReplicationConfig config;
    private MessageDispatcher dispatcher;
    private WebSocket webSocket;
    private LocalReplica replica;


    public MessageTemplate(ReplicationConfig config, LocalReplica replica) {
        this.config = config;
        this.replica = replica;
    }

    private String getReplicaId() {
        return replica.getReplicaId();
    }

    public void sendMessage(DataGateMessage message) {
        try {
            if (webSocket != null) {
                ObjectMapper objectMapper = config.getObjectMapper();
                String asString = objectMapper.writeValueAsString(message);
                webSocket.send(asString);
            }
        } catch (JsonProcessingException jpe) {
            log.error("Failed to create json message {}", message, jpe);
            throw new ReplicationException("invalid message provided", jpe);
        }
    }

    public void openConnection() {
        try {
            OkHttpClient client = createClient();
            Request.Builder builder = config.getRequestBuilder();
            // Server will keep track of Initiator and guarantee message correctness
            builder.addHeader("Initiator", getReplicaId());
            Request request = builder.build();

            this.dispatcher = new MessageDispatcher(config, replica);
            webSocket = client.newWebSocket(request, this.dispatcher);
        } catch (Exception e) {
            log.error("Error while establishing connection from {}", getReplicaId(), e);
            throw new ReplicationException("failed to open connection to server", e);
        }
    }

    public void closeConnection(String reason) {
        if (webSocket != null) {
            webSocket.close(1000, reason);
            webSocket = null;
        }
    }

    private OkHttpClient createClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeout().getTime(),
                config.getConnectTimeout().getTimeUnit())
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false);

        if (config.getProxy() != null) {
            builder.proxy(config.getProxy());
        }

        if (config.isAcceptAllCertificates()) {
            try {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
                };

                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                throw new ReplicationException("error while configuring SSLSocketFactory", e);
            }
        }

        return builder.build();
    }
}
