package org.dizitart.no2.sync;

import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.sync.connection.AuthType;
import org.dizitart.no2.sync.connection.ConnectionConfig;
import org.dizitart.no2.sync.connection.TimeSpan;
import org.dizitart.no2.sync.connection.WebSocketConfig;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author Anindya Chatterjee.
 */
public class ReplicaBuilder {
    private NitriteCollection collection;
    private String replicationServer;
    private String jwtToken;
    private String basicToken;
    private TimeSpan connectTimeout = new TimeSpan(5, TimeUnit.SECONDS);
    private Integer changesPerMessage = 10;

    ReplicaBuilder() {
    }

    public ReplicaBuilder of(NitriteCollection collection) {
        this.collection = collection;
        return this;
    }

    public ReplicaBuilder of(ObjectRepository<?> repository) {
        return of(repository.getDocumentCollection());
    }

    public ReplicaBuilder remote(String replicationServer) {
        this.replicationServer = replicationServer;
        return this;
    }

    public ReplicaBuilder auth(String authToken) {
        this.jwtToken = authToken;
        return this;
    }

    public ReplicaBuilder auth(String userName, String password) {
        this.basicToken = toHex(userName + ":" + password);
        return this;
    }

    public ReplicaBuilder connectTimeout(TimeSpan timeSpan) {
        this.connectTimeout = timeSpan;
        return this;
    }

    public ReplicaBuilder changesPerMessage(Integer size) {
        this.changesPerMessage = size;
        return this;
    }

    public Replica create() {
        if (collection != null) {
            ConnectionConfig connectionConfig = createConfig();
            ReplicationConfig config = new ReplicationConfig();
            config.setCollection(collection);
            config.setChangesPerMessage(changesPerMessage);
            config.setConnectionConfig(connectionConfig);

            Replica replica = new Replica(config);
            this.collection.subscribe(replica);
            return replica;
        } else {
            throw new ReplicationException("no collection or repository has been specified for replication");
        }
    }

    private ConnectionConfig createConfig() {
        if (!StringUtils.isNullOrEmpty(replicationServer)) {
            if (replicationServer.startsWith("ws")) {
                WebSocketConfig webSocketConfig = new WebSocketConfig();
                webSocketConfig.setUrl(replicationServer);
                webSocketConfig.setConnectTimeout(connectTimeout);

                if (!StringUtils.isNullOrEmpty(jwtToken)) {
                    webSocketConfig.setAuthType(AuthType.Jwt);
                    webSocketConfig.setAuthToken(jwtToken);
                } else if (!StringUtils.isNullOrEmpty(basicToken)) {
                    webSocketConfig.setAuthType(AuthType.Basic);
                    webSocketConfig.setAuthToken(basicToken);
                } else {
                    webSocketConfig.setAuthType(AuthType.None);
                }

                return webSocketConfig;
            } else {
                throw new ReplicationException("only websocket connection is supported");
            }
        } else {
            throw new ReplicationException("replication server url is required");
        }
    }

    private String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(StandardCharsets.UTF_8)));
    }
}
