package org.dizitart.no2.sync.message;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Anindya Chatterjee.
 */
public enum MessageType {
    Error("no2.sync.error"),
    Connect("no2.sync.connect"),
    ConnectAck("no2.sync.connect.ack"),
    Disconnect("no2.sync.disconnect"),
    DisconnectAck("no2.sync.disconnect.ack"),
    Checkpoint("no2.sync.checkpoint"),
    BatchChangeStart("no2.sync.batch.start"),
    BatchChangeContinue("no2.sync.batch.continue"),
    BatchChangeEnd("no2.sync.batch.end"),
    BatchAck("no2.sync.batch.ack"),
    BatchEndAck("no2.sync.batch.end.ack"),
    DataGateFeed("no2.sync.feed"),
    DataGateFeedAck("no2.sync.feed.ack");

    private String code;

    MessageType(String code) {
        this.code = code;
    }

    @JsonValue
    public String code() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}
