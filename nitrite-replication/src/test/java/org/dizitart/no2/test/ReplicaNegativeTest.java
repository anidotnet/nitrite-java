package org.dizitart.no2.test;

import org.dizitart.no2.common.concurrent.ThreadPoolManager;
import org.dizitart.no2.test.server.SimpleDataGateServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.dizitart.no2.test.ReplicaTest.getRandomTempDbFile;

/**
 * @author Anindya Chatterjee
 */
public class ReplicaNegativeTest {
    private SimpleDataGateServer server;
    private String dbFile;
    private ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        dbFile = getRandomTempDbFile();
        server = new SimpleDataGateServer(9090);
        executorService = ThreadPoolManager.getThreadPool(2, "ReplicaNegativeTest");
        server.start();
    }

    @After
    public void cleanUp() throws IOException, InterruptedException {
        if (executorService != null) {
            executorService.shutdown();
        }
        server.stop();
    }

    @Test
    public void testSingleUserMultiReplicaSameCollection() {

    }
}
