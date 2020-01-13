package org.dizitart.no2.sync.connection;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
public class WebSocketConfig implements ConnectionConfig {
    private String url;
    private String authToken;
    private AuthType authType;
    private Integer connectTimeout;
}
