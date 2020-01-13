package org.dizitart.no2.sync.message;

/**
 * @author Anindya Chatterjee.
 */
public enum MessageType {
    LocalChangeStart("no2.sync.local.start"),
    LocalChangeContinue("no2.sync.local.continue"),
    LocalChangeEnd("no2.sync.local.end"),
    Feed("no2.sync.feed"),

    ;

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
