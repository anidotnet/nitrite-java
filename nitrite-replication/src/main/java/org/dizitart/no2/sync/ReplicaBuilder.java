package org.dizitart.no2.sync;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.collection.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.sync.crdt.LastWriteWinRegister;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Anindya Chatterjee.
 */
public class ReplicaBuilder {
    private static final String REPLICA = "replica";

    private NitriteCollection collection;
    private NitriteStore nitriteStore;
    private String replicationServer;

    ReplicaBuilder() {
    }

    public ReplicaBuilder of(NitriteCollection collection) {
        this.collection = collection;
        return this;
    }

    public ReplicaBuilder of(ObjectRepository<?> repository) {
        return of(repository.getDocumentCollection());
    }

    public ReplicaBuilder withStore(NitriteStore store) {
        this.nitriteStore = store;
        return this;
    }

    public Replica build() {
        if (collection != null) {
            Attributes attributes = getAttributes();
            String replicaName = getReplicaName(attributes);
            saveAttributes(attributes);

            if (nitriteStore != null) {
                NitriteMap<NitriteId, LastWriteWinRegister<Document>> replicaMap
                    = nitriteStore.openMap(replicaName);
                Replica replica = new Replica(collection, replicaMap);
                this.collection.subscribe(replica);

                WebSocket webSocket = openWebsocket();

                return replica;
            } else {
                throw new ReplicationException("no store has been configured");
            }
        } else {
            throw new ReplicationException("no collection or repository has been specified for replication");
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

    private WebSocket openWebsocket() {
        try {
            WebSocketFactory factory = new WebSocketFactory();
            factory.setConnectionTimeout(5000);
            return factory.createSocket(replicationServer);
        } catch (IOException ioe) {

        }
    }
}
