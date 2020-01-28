package org.dizitart.no2.test.server;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.common.util.StringUtils;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class SimpleDataGateServerConfig extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        String replicaId = getReplicaId(sec, request);
        String authorization = getAuthorization(sec, request);

        if (StringUtils.isNullOrEmpty(replicaId)) {
            sec.getUserProperties().put("error", "no replica identifier provided in handshake request");
            log.error("no replica identifier provided in handshake request");
            return;
        }

        if (StringUtils.isNullOrEmpty(authorization)) {
            sec.getUserProperties().put("error", "websocket connection not authorized from " + replicaId);
            log.error("Websocket connection not authorized");
            return;
        }

        if (authorization.equals("Bearer abcd")) return;

        sec.getUserProperties().put("error", "invalid token provided from " + replicaId);
        log.error("Invalid token provided from {}", replicaId);
    }

    private String getReplicaId(ServerEndpointConfig sec, HandshakeRequest request) {
        Map<String, List<String>> headers = request.getHeaders();
        if (headers.containsKey("Replica")
            && headers.get("Replica") != null
            && headers.get("Replica").size() > 0) {
            String replicaId = headers.get("Replica").get(0);
            sec.getUserProperties().put("Replica", replicaId);
            return replicaId;
        }
        return null;
    }

    private String getAuthorization(ServerEndpointConfig sec, HandshakeRequest request) {
        Map<String, List<String>> headers = request.getHeaders();
        if (headers.containsKey("Authorization")
            && headers.get("Authorization") != null
            && headers.get("Authorization").size() > 0) {
            String authorization = headers.get("Authorization").get(0);
            sec.getUserProperties().put("Authorization", authorization);
            return authorization;
        }
        return null;
    }

}
