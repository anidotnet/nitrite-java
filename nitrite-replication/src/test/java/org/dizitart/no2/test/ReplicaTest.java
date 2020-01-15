package org.dizitart.no2.test;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.sync.Replica;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class ReplicaTest {
    private MockDataGateServer server;
    private String dbFile;

    @Before
    public void setUp() {
        dbFile = getRandomTempDbFile();
        server = new MockDataGateServer();
        server.buildAndStartServer(9090, "127.0.0.1");
    }

    @After
    public void cleanUp() {
        server.stop();
    }

    @Test
    public void testReplica() {
        Nitrite db = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        NitriteCollection collection = db.getCollection("replicate-test");
        Document document = Document.createDocument().put("firstName", "Anindya")
            .put("lastName", "Chatterjee");
        collection.insert(document);

        Replica replica = Replica.builder()
            .of(collection)
            .remote("ws://127.0.0.1:9090/datagate")
            .jwtAuth("anidotnet", "abcd")
            .create();

        replica.connect();
        System.out.println("replica connected");
        collection.remove(document);
        await().atMost(5, SECONDS).until(() -> server.getCollectionReplicaMap().size() == 1);
        await().atMost(5, SECONDS).until(() -> server.getUserReplicaMap().size() == 1);
        await().atMost(5, SECONDS).until(() -> server.getUserReplicaMap().containsKey("anidotnet"));
        await().atMost(5, SECONDS).until(() -> server.getCollectionReplicaMap().containsKey("anidotnet@replicate-test"));
        await().atMost(5, SECONDS).until(() -> {
            LastWriteWinMap lastWriteWinMap = server.getReplicaStore().get("anidotnet@replicate-test");
            Document doc = lastWriteWinMap.getCollection().find(Filter.byId(document.getId())).firstOrNull();
            return doc == null;
        });

    }

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }
}
