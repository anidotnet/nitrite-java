package org.dizitart.no2.test.server;

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anindya Chatterjee
 */
@Data
public class Repository {
    private static Repository instance = new Repository();
    private Map<String, List<String>> collectionReplicaMap;
    private Map<String, List<String>> userReplicaMap;
    private Map<String, LastWriteWinMap> replicaStore;
    private Nitrite db;
    private String serverId;
//    private Map<String, Session> sessionMap;

    private Repository() {
        reset();
    }

    public static Repository getInstance() {
        return instance;
    }

    public void reset() {
        collectionReplicaMap = new ConcurrentHashMap<>();
        userReplicaMap = new ConcurrentHashMap<>();
        replicaStore = new ConcurrentHashMap<>();
//        sessionMap = new ConcurrentHashMap<>();

        db = NitriteBuilder.get().openOrCreate();
        serverId = UUID.randomUUID().toString();
    }
}
