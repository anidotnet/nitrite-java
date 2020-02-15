package org.dizitart.no2.test.server;

import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;

import javax.websocket.Session;
import java.util.*;
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
    private Set<Session> authorizedSessions;
    private Map<String, String> userMap;

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
        authorizedSessions = new HashSet<>();
        userMap = new ConcurrentHashMap<>();

        db = NitriteBuilder.get().openOrCreate();
        serverId = UUID.randomUUID().toString();
    }
}
