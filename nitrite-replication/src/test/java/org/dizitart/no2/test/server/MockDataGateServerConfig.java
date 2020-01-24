package org.dizitart.no2.test.server;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

/**
 * @author Anindya Chatterjee
 */
public class MockDataGateServerConfig extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        Map<String, List<String>> headers = request.getHeaders();
        if (!headers.containsKey("Authorization")) {
            throw new SecurityException("connection not authorized");
        }

        String authorization = headers.get("Authorization").get(0);
        if (authorization.equals("Bearer abcd")) return;

        throw new SecurityException("invalid token");
    }
}
