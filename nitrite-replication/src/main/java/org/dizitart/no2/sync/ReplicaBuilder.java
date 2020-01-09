package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.sync.connection.*;
import org.dizitart.no2.sync.crdt.LastWriteWinRegister;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Anindya Chatterjee.
 */
public class ReplicaBuilder {
    private static final String REPLICA = "replica";

    private NitriteCollection collection;
    private NitriteStore nitriteStore;
    private String replicationServer;
    private String jwtToken;
    private String basicToken;
    private TimeSpan connectTimeout = new TimeSpan(5, TimeUnit.SECONDS);

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

    public ReplicaBuilder withStore(NitriteStore store) {
        this.nitriteStore = store;
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

    public Replica create() {
        if (collection != null) {
            Attributes attributes = getAttributes();
            String replicaName = getReplicaName(attributes);
            saveAttributes(attributes);

            if (nitriteStore != null) {
                NitriteMap<NitriteId, LastWriteWinRegister<Document>> replicaMap
                    = nitriteStore.openMap(replicaName);
                Replica replica = new Replica(collection, replicaMap);
                this.collection.subscribe(replica);

                ConnectionConfig connectionConfig = createConfig();
                replica.connectionConfig(connectionConfig);
                return replica;
            } else {
                throw new ReplicationException("no store has been configured");
            }
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

    private String getReplicaName(Attributes attributes) {
        String replica = attributes.get(REPLICA);
        if (StringUtils.isNullOrEmpty(replica)) {
            replica = UUID.randomUUID().toString();
            attributes.set(REPLICA, replica);
        }
        return replica;
    }

    private Attributes getAttributes() {
        Attributes attributes = collection.getAttributes();
        if (attributes == null) {
            attributes = new Attributes();
        }
        return attributes;
    }

    private void saveAttributes(Attributes attributes) {
        collection.setAttributes(attributes);
    }

    private String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(StandardCharsets.UTF_8)));
    }
}
