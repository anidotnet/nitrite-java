package org.dizitart.no2.sync.message;

import org.dizitart.no2.sync.crdt.LastWriteWinState;

/**
 * @author Anindya Chatterjee
 */
public interface ChangeMessage {
    LastWriteWinState getChanges();
}
