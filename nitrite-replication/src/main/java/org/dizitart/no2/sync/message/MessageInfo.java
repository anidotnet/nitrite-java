package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class MessageInfo {
    private String collection;
    private String userName;
    private Long timestamp;
    private MessageType messageType;
    private String server;
    private String replicaId;
}
