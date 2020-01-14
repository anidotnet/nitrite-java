package org.dizitart.no2.sync.message;

/**
 * @author Anindya Chatterjee.
 */
public enum MessageType {
    Connect("no2.sync.connect"),
    Disconnect("no2.sync.disconnect"),
    BatchChangeStart("no2.sync.batch.start"),
    BatchChangeContinue("no2.sync.batch.continue"),
    BatchChangeEnd("no2.sync.batch.end"),
    Feed("no2.sync.feed");

    private String code;

    MessageType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    @Override
    public String toString() {
        return code();
    }
}
