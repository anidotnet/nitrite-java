package org.dizitart.no2.test;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.sync.Replica;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.test.ReplicaTest.getRandomTempDbFile;
import static org.dizitart.no2.test.TestUtils.randomDocument;

/**
 * @author Anindya Chatterjee
 */
public class ReplicaNegativeTest {
    private MockDataGateServer server;
    private String dbFile;
    private ExecutorService executorService;

    @Before
    public void setUp() {
        dbFile = getRandomTempDbFile();
        server = new MockDataGateServer(9090, "127.0.0.1");
        executorService = Executors.newFixedThreadPool(2);
    }

    @After
    public void cleanUp() {
        server.stop();
    }

    @Test
    public void testSingleUserMultiReplicaSameCollection() {
        Nitrite db = NitriteBuilder.get()
            .openOrCreate();
        NitriteCollection collection = db.getCollection("replicate-test");

        Replica r1 = Replica.builder()
            .of(collection)
            .remote("ws://127.0.0.1:9090/datagate")
            .jwtAuth("anidotnet", "abcd")
            .create();
        r1.connect();

        Replica r2 = Replica.builder()
            .of(collection)
            .remote("ws://127.0.0.1:9090/datagate")
            .jwtAuth("anidotnet", "abcd")
            .create();
        r2.connect();

        Replica r3 = Replica.builder()
            .of(collection)
            .remote("ws://127.0.0.1:9090/datagate")
            .jwtAuth("anidotnet", "abcd")
            .create();
        r3.connect();

        Random random = new Random();
        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                collection.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Assert.fail("Thread interrupted");
                }
            }
        });

        await().atMost(10, SECONDS).until(() -> collection.size() == 10);
    }
}
