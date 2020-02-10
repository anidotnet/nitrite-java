package org.dizitart.no2.test.server;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class SimpleDataGateServerConfig extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {

    }
}
