package org.dizitart.no2.sync.message;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class MessageHeader {
    private String id;
    private String correlationId;
    private String collection;
    private String userName;
    private Long timestamp;
    private MessageType messageType;
    private String origin;
}
