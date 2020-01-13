package org.dizitart.no2.sync.message;

/**
 * @author Anindya Chatterjee.
 */
public enum MessageType {
    LocalChangeStart("no2.sync.local.start"),
    LocalChangeContinue("no2.sync.local.continue"),
    LocalChangeEnd("no2.sync.local.end"),

    Feed("no2.sync.feed"),
    Checkpoint("no2.sync.checkpoint"),
    ChangeResponse("no2.sync.change-response");

    private String code;

    private MessageType(String code) {
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
