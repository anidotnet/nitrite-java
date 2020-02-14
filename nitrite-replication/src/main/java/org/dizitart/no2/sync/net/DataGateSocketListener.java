package org.dizitart.no2.sync.net;

import okhttp3.Response;
import okio.ByteString;

/**
 * @author Anindya Chatterjee
 */
public interface DataGateSocketListener {
    default void onOpen(Response response) {

    }

    default void onMessage(String text) {

    }

    default void onMessage(ByteString bytes) {

    }

    default void onReconnect() {

    }

    default void onClosing(int code, String reason) {

    }

    default void onClosed(int code, String reason) {

    }

    default void onFailure(Throwable error, Response response) {

    }
}
