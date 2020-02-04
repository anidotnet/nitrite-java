package org.dizitart.no2.sync.handlers;

import okhttp3.WebSocket;
import org.dizitart.no2.sync.message.DataGateMessage;

/**
 * @author Anindya Chatterjee
 */
public interface MessageHandler<M extends DataGateMessage> {
    void handleMessage(WebSocket webSocket, M message) throws Exception;
}
