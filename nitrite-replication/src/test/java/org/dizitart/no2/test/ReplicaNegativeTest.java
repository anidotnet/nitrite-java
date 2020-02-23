package org.dizitart.no2.test;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.sync.Replica;
import org.dizitart.no2.sync.crdt.LastWriteWinMap;
import org.dizitart.no2.test.server.Repository;
import org.dizitart.no2.test.server.SimpleDataGateServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.test.ReplicaTest.getRandomTempDbFile;
import static org.dizitart.no2.test.TestUtils.randomDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anindya Chatterjee
 */
public class ReplicaNegativeTest {
    private SimpleDataGateServer server;
    private String dbFile;
    private ExecutorService executorService;
    private Repository repository;

    @Before
    public void setUp() throws Exception {
        dbFile = getRandomTempDbFile();
        server = new SimpleDataGateServer(9090);
        executorService = ThreadPoolManager.getThreadPool(2, "ReplicaNegativeTest");
        server.start();
        repository = Repository.getInstance();
    }

    @After
    public void cleanUp() {
        if (executorService != null) {
            executorService.shutdown();
        }
        server.stop();
    }

    @Test
    public void testServerClose() {
        repository.getUserMap().put("anidotnet", "abcd");

        Nitrite db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();

        NitriteCollection c1 = db1.getCollection("testServerClose");

        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate/anidotnet/testServerClose")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
            }
        });

        await().atMost(5, SECONDS).until(() -> repository.getCollectionReplicaMap().size() == 1);
        assertEquals(repository.getUserReplicaMap().size(), 1);
        assertTrue(repository.getUserReplicaMap().containsKey("anidotnet"));
        assertTrue(repository.getCollectionReplicaMap().containsKey("anidotnet@testServerClose"));
        LastWriteWinMap lastWriteWinMap = repository.getReplicaStore().get("anidotnet@testServerClose");

        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().find().size() == 10);
        server.stop();
        await().atMost(5, SECONDS).until(() -> !r1.isConnected());
    }

    /*
    * 1. Server close and again restarted
    * 2. Connectivity check with with atomic counter
    * */
}
