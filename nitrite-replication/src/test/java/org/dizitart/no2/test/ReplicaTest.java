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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.dizitart.no2.collection.Document.createDocument;
import static org.dizitart.no2.common.util.DocumentUtils.isSimilar;
import static org.dizitart.no2.filters.FluentFilter.where;
import static org.dizitart.no2.test.TestUtils.randomDocument;
import static org.junit.Assert.*;

/**
 * @author Anindya Chatterjee
 */
public class ReplicaTest {
    private MockDataGateServer server;
    private String dbFile;
    private ExecutorService executorService;

    @Before
    public void setUp() {
        dbFile = getRandomTempDbFile();
        server = new MockDataGateServer();
        server.buildAndStartServer(9090, "127.0.0.1");
        executorService = Executors.newFixedThreadPool(2);
    }

    @After
    public void cleanUp() throws IOException {
        server.stop();
        server = null;
        Files.delete(Paths.get(dbFile));
    }

    @Test
    public void testSingleUserSingleReplica() {
        Nitrite db = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();
        NitriteCollection collection = db.getCollection("replicate-test");
        Document document = createDocument().put("firstName", "Anindya")
            .put("lastName", "Chatterjee")
            .put("address", createDocument("street", "1234 Abcd Street")
                .put("pin", 123456));
        collection.insert(document);

        Replica replica = Replica.builder()
            .of(collection)
            .remote("ws://127.0.0.1:9090/datagate")
            .jwtAuth("anidotnet", "abcd")
            .create();

        replica.connect();

        await().atMost(5, SECONDS).until(() -> server.getCollectionReplicaMap().size() == 1);
        assertEquals(server.getUserReplicaMap().size(), 1);
        assertTrue(server.getUserReplicaMap().containsKey("anidotnet"));
        assertTrue(server.getCollectionReplicaMap().containsKey("anidotnet@replicate-test"));
        LastWriteWinMap lastWriteWinMap = server.getReplicaStore().get("anidotnet@replicate-test");

        Document doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();

        assertTrue(isSimilar(document, doc, "firstName", "lastName", "address", "pin"));

        collection.remove(doc);
        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().size() == 0);
        doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();
        assertNull(doc);
        assertEquals(collection.size(), 0);

        collection.insert(document);
        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().size() == 1);
        doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();
        assertTrue(isSimilar(document, doc, "firstName", "lastName", "address", "pin"));

        replica.disconnect();
        collection.remove(doc);
        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().size() == 1);
        doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();
        assertTrue(isSimilar(document, doc, "firstName", "lastName", "address", "pin"));

        replica.connect();
        await().atMost(5, SECONDS).until(() -> lastWriteWinMap.getCollection().size() == 0);
        doc = lastWriteWinMap.getCollection().find(where("firstName").eq("Anindya")).firstOrNull();
        assertNull(doc);
    }

    @Test
    public void testSingleUserMultiReplica() {
        Nitrite db1 = NitriteBuilder.get()
            .filePath(dbFile)
            .openOrCreate();

        Nitrite db2 = NitriteBuilder.get()
            .openOrCreate();

        NitriteCollection c1 = db1.getCollection("replicate-test");
        NitriteCollection c2 = db2.getCollection("replicate-test");

        Replica r1 = Replica.builder()
            .of(c1)
            .remote("ws://127.0.0.1:9090/datagate")
            .jwtAuth("anidotnet", "abcd")
            .create();

        Replica r2 = Replica.builder()
            .of(c2)
            .remote("ws://127.0.0.1:9090/datagate")
            .jwtAuth("anidotnet", "abcd")
            .create();

        r1.connect();

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
            }
        });

        await().atMost(5, SECONDS).until(() -> c1.size() == 10);
        assertEquals(c2.size(), 0);

        r2.connect();
        System.out.println(r2.getReplicaId() + " connected");
        await().atMost(5, SECONDS).until(() -> {
            LastWriteWinMap lastWriteWinMap = server.getReplicaStore().get("anidotnet@replicate-test");
            System.out.println("server - " + lastWriteWinMap.getCollection().size());
            System.out.println("server tombstones - " + lastWriteWinMap.getTombstones().size());
            System.out.println("local - " + c2.size());
            return c2.size() == 10;
        });

        Random random = new Random();
        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        executorService.submit(() -> {
            for (int i = 0; i < 20; i++) {
                Document document = randomDocument();
                c2.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        await().atMost(10, SECONDS).until(() -> c1.size() == 40);
        assertEquals(c2.size(), 40);

        r1.disconnect();

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                Document document = randomDocument();
                c1.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        executorService.submit(() -> {
            for (int i = 0; i < 20; i++) {
                Document document = randomDocument();
                c2.insert(document);
                try {
                    Thread.sleep(random.nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        r1.connect();

        await().atMost(10, SECONDS).until(() -> c1.size() == 70);
        TestUtils.assertEquals(c1, c2);

        executorService.submit(() -> {
            c2.remove(Filter.ALL);
        });

        await().atMost(10, SECONDS).until(() -> c2.size() == 0);
        await().atMost(5, SECONDS).until(() -> {
            System.out.println(c1.size());
            LastWriteWinMap lastWriteWinMap = server.getReplicaStore().get("anidotnet@replicate-test");
            System.out.println("server - " + lastWriteWinMap.getCollection().size());
            System.out.println("server tombstones - " + lastWriteWinMap.getTombstones().size());
            return c1.size() == 0;
        });
        TestUtils.assertEquals(c1, c2);
    }

//    @Test
    public void testMultiUserSingleReplica() {
        Nitrite db = NitriteBuilder.get()
            .filePath(dbFile)
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
        r1.connect();

        Replica r3 = Replica.builder()
            .of(collection)
            .remote("ws://127.0.0.1:9090/datagate")
            .jwtAuth("anidotnet", "abcd")
            .create();
        r1.connect();
    }

    public static String getRandomTempDbFile() {
        String dataDir = System.getProperty("java.io.tmpdir") + File.separator + "nitrite" + File.separator + "data";
        File file = new File(dataDir);
        if (!file.exists()) {
            assertTrue(file.mkdirs());
        }
        return file.getPath() + File.separator + UUID.randomUUID().toString() + ".db";
    }

    /*
     * Test case
     *
     * 1. Single user, one replica
     * 2. Single user, two replica, random write using threads, connect - disconnect
     * 3. Multi-user, one replica
     * 4. Multi-user, multiple collection
     * 5. Single user, two replicas, two servers (each for one replica)
     * 6. Handle security for jwt tokens - extract user info only from jwt token
     * */
}
