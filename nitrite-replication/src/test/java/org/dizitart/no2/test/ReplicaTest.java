package org.dizitart.no2.test;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.sync.Replica;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

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

    @Test
    public void testReplica() throws InterruptedException {
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
        Thread.sleep(5000);
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
