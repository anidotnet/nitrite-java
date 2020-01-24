package org.dizitart.no2.test.server;


import org.glassfish.tyrus.server.Server;

/**
 * @author Anindya Chatterjee
 */
public class MockDataGateServer {
    private int port;
    private Server server;
    private Repository repository;

    public MockDataGateServer(int port) {
        this.port = port;
        this.repository = Repository.getInstance();
    }

    public void start() throws Exception {
        server = new Server("127.0.0.1", port, "", null, MockDataGateEndpoint.class);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));
    }

    public void stop() {
        server.stop();
        repository.reset();
    }
}
