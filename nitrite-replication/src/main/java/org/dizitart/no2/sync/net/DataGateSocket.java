package org.dizitart.no2.sync.net;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.dizitart.no2.sync.Config;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.message.DataGateMessage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class DataGateSocket {
    private final static int RECONNECT_INTERVAL = 10 * 1000;
    private final static long RECONNECT_MAX_TIME = 120 * 1000;

    private WebSocket mWebSocket;
    private OkHttpClient httpClient;
    private Request request;
    private int currentStatus = Status.DISCONNECTED;
    private boolean manualClose;
    private DataGateSocketListener listener;
    private Lock lock;
    private int reconnectCount = 0;
    private Timer reconnectTimer;
    private ObjectMapper objectMapper;
    private String url;
    private Config config;
    private WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, final Response response) {
            mWebSocket = webSocket;
            setCurrentStatus(Status.CONNECTED);
            connected();
            if (listener != null) {
                listener.onOpen(response);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            if (listener != null) {
                listener.onMessage(text);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if (listener != null) {
                listener.onMessage(bytes);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            if (listener != null) {
                listener.onClosing(code, reason);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            if (listener != null) {
                listener.onClosed(code, reason);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (listener != null) {
                listener.onFailure(t, response);
            }
        }
    };

    public DataGateSocket(Config config) {
        this.config = config;
        this.url = config.getServer();
        this.lock = new ReentrantLock();
        this.httpClient = createClient();
        this.request = config.getRequestBuilder().build();
        this.objectMapper = config.getObjectMapper();
    }

    private void initWebSocket() {
        if (httpClient != null) {
            httpClient.dispatcher().cancelAll();
        }
        try {
            lock.lockInterruptibly();
            try {
                httpClient.newWebSocket(request, webSocketListener);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException ignored) {
        }
    }

    public void setListener(DataGateSocketListener listener) {
        this.listener = listener;
    }

    public synchronized boolean isConnected() {
        return currentStatus == Status.CONNECTED;
    }

    public synchronized int getCurrentStatus() {
        return currentStatus;
    }

    public synchronized void setCurrentStatus(int status) {
        this.currentStatus = status;
    }

    public void startConnect() {
        manualClose = false;
        buildConnect();
    }

    public void stopConnect() {
        manualClose = true;
        disconnect();
    }

    public boolean sendMessage(DataGateMessage message) {
        boolean isSent = false;
        try {
            if (mWebSocket != null && currentStatus == Status.CONNECTED) {
                String text = objectMapper.writeValueAsString(message);
                isSent = mWebSocket.send(text);

                if (!isSent) {
                    tryReconnect();
                }
            }
        } catch (Exception e) {
            log.error("Error while sending message", e);
            isSent = false;
        }
        return isSent;
    }

    private OkHttpClient createClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout().getTime(),
                config.getTimeout().getTimeUnit())
            .readTimeout(config.getTimeout().getTime(),
                config.getTimeout().getTimeUnit())
            .writeTimeout(config.getTimeout().getTime(),
                config.getTimeout().getTimeUnit())
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
                throw new ReplicationException("error while configuring SSLSocketFactory", e, true);
            }
        }

        return builder.build();
    }

    private void tryReconnect() {
        if (manualClose) {
            return;
        }

        if (isNetworkDisconnected()) {
            setCurrentStatus(Status.DISCONNECTED);
            return;
        }

        setCurrentStatus(Status.RECONNECT);
        reconnectTimer = new Timer();

        long delay = reconnectCount * RECONNECT_INTERVAL;
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onReconnect();
                }
                buildConnect();
            }
        }, Math.min(delay, RECONNECT_MAX_TIME));
        reconnectCount++;
    }

    private void cancelReconnect() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
        }
        reconnectCount = 0;
    }

    private void connected() {
        cancelReconnect();
    }

    private void disconnect() {
        if (currentStatus == Status.DISCONNECTED) {
            return;
        }

        cancelReconnect();
        if (httpClient != null) {
            httpClient.dispatcher().cancelAll();
        }

        if (mWebSocket != null) {
            boolean isClosed = mWebSocket.close(Status.CODE.NORMAL_CLOSE, Status.TIP.NORMAL_CLOSE);
            if (!isClosed) {
                if (listener != null) {
                    listener.onClosed(Status.CODE.ABNORMAL_CLOSE, Status.TIP.ABNORMAL_CLOSE);
                }
            }
        }

        setCurrentStatus(Status.DISCONNECTED);
    }

    private synchronized void buildConnect() {
        if (isNetworkDisconnected()) {
            setCurrentStatus(Status.DISCONNECTED);
            return;
        }

        switch (getCurrentStatus()) {
            case Status.CONNECTED:
            case Status.CONNECTING:
                break;
            default:
                setCurrentStatus(Status.CONNECTING);
                initWebSocket();
        }
    }

    private boolean isNetworkDisconnected() {
        try {
            InetAddress[] addresses = InetAddress.getAllByName("127.0.0.1");
            for (InetAddress address : addresses) {
                if (address.isReachable(2000)) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Connection failed to {}", url, e);
            return true;
        }
        return true;
    }
}
