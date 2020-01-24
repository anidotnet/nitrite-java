package org.dizitart.no2.test.server;


import org.glassfish.tyrus.server.Server;

/**
 * @author Anindya Chatterjee
 */
public class SimpleDataGateServer {
    private int port;
    private Server server;
    private Repository repository;

    public SimpleDataGateServer(int port) {
        this.port = port;
        this.repository = Repository.getInstance();
    }

    public void start() throws Exception {
        server = new Server("127.0.0.1", port, "", null, SimpleDataGateEndpoint.class);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop()));
    }

    public void stop() {
        server.stop();
        repository.reset();
    }
}
