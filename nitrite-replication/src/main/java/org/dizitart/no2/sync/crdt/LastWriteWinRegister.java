package org.dizitart.no2.sync.crdt;

import lombok.Data;
import org.dizitart.no2.collection.Document;

import static org.dizitart.no2.common.Constants.DOC_MODIFIED;

/**
 * @author Anindya Chatterjee
 */
@Data
public class LastWriteWinRegister {
    private Document state;

    public LastWriteWinRegister(Document value) {
        this.state = value;
    }

    public LastWriteWinRegister(Document value, long timestamp) {
        this.state = value;
        this.state.put(DOC_MODIFIED, timestamp);
    }

    public Document get() {
        return state;
    }

    public void tombstone(long timestamp) {
        if (applicable(timestamp)) {

        }
    }

    public void merge(Document state) {
        if (applicable(state.getLastModifiedSinceEpoch())) {
            this.state = state;
        }
    }

    public void set(Document value, long timestamp) {
        if (applicable(timestamp)) {
            this.state = value.clone().put(DOC_MODIFIED, timestamp);
        }
    }

    private boolean applicable(long timestamp) {
        return this.state.getLastModifiedSinceEpoch() <= timestamp;
    }
}
