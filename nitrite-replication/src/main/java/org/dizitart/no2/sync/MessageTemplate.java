package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.sync.message.DataGateMessage;
import org.dizitart.no2.sync.net.DataGateSocket;

/**
 * @author Anindya Chatterjee
 */
@Slf4j
public class MessageTemplate implements AutoCloseable {
    private Config config;
    private ReplicationTemplate replica;

    @Getter
    private DataGateSocket dataGateSocket;

    public MessageTemplate(Config config, ReplicationTemplate replica) {
        this.config = config;
        this.replica = replica;
    }

    private String getReplicaId() {
        return replica.getReplicaId();
    }

    public void sendMessage(DataGateMessage message) {
        if (dataGateSocket != null && dataGateSocket.isConnected()) {
            if (!dataGateSocket.sendMessage(message)) {
                throw new ReplicationException("failed to deliver message " + message, true);
            }
        }
    }

    public void openConnection() {
        try {
            dataGateSocket = new DataGateSocket(config);
            MessageDispatcher dispatcher = new MessageDispatcher(config, replica);

            dataGateSocket.setListener(dispatcher);
            dataGateSocket.startConnect();
        } catch (Exception e) {
            log.error("Error while establishing connection from {}", getReplicaId(), e);
            throw new ReplicationException("failed to open connection to server", e, true);
        }
    }

    public void closeConnection(String reason) {
        if (dataGateSocket != null) {
            dataGateSocket.stopConnect(reason);
        }
    }

    @Override
    public void close() {
        if (dataGateSocket != null) {
            dataGateSocket.stopConnect("normal close");
        }
    }
}
