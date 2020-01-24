package org.dizitart.no2.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.sync.module.DocumentModule;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author Anindya Chatterjee.
 */
public class ReplicaBuilder {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic ";
    private static final String BEARER = "Bearer ";

    private NitriteCollection collection;
    private String replicationServer;
    private String jwtToken;
    private String basicToken;
    private TimeSpan connectTimeout;
    private TimeSpan debounce;
    private Integer chunkSize;
    private String userName;
    private ObjectMapper objectMapper;

    ReplicaBuilder() {
        chunkSize = 10;
        connectTimeout = new TimeSpan(5, TimeUnit.SECONDS);
        debounce = new TimeSpan(1, TimeUnit.SECONDS);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new DocumentModule());
    }

    public ReplicaBuilder of(NitriteCollection collection) {
        this.collection = collection;
        return this;
    }

    public ReplicaBuilder of(ObjectRepository<?> repository) {
        return of(repository.getDocumentCollection());
    }

    public ReplicaBuilder remote(String replicationServer) {
        this.replicationServer = replicationServer;
        return this;
    }

    public ReplicaBuilder jwtAuth(String userName, String authToken) {
        this.jwtToken = authToken;
        this.userName = userName;
        return this;
    }

    public ReplicaBuilder basicAuth(String userName, String password) {
        this.basicToken = toHex(userName + ":" + password);
        this.userName = userName;
        return this;
    }

    public ReplicaBuilder connectTimeout(TimeSpan timeSpan) {
        this.connectTimeout = timeSpan;
        return this;
    }

    public ReplicaBuilder chunkSize(Integer size) {
        this.chunkSize = size;
        return this;
    }

    public ReplicaBuilder debounce(TimeSpan timeSpan) {
        this.debounce = timeSpan;
        return this;
    }

    public ReplicaBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public Replica create() {
        if (collection != null) {
            Request.Builder builder = createRequestBuilder();

            ReplicationConfig config = new ReplicationConfig();
            config.setCollection(collection);
            config.setChunkSize(chunkSize);
            config.setUserName(userName);
            config.setDebounce(getTimeoutInMillis(debounce));
            config.setObjectMapper(objectMapper);
            config.setConnectTimeout(connectTimeout);
            config.setRequestBuilder(builder);

            return new Replica(config);
        } else {
            throw new ReplicationException("no collection or repository has been specified for replication");
        }
    }

    private Request.Builder createRequestBuilder() {
        Request.Builder builder = new Request.Builder();
        if (!StringUtils.isNullOrEmpty(jwtToken)) {
            builder.addHeader(AUTHORIZATION, BEARER + jwtToken);
        } else if (!StringUtils.isNullOrEmpty(basicToken)) {
            builder.addHeader(AUTHORIZATION, BASIC + basicToken);
        }

        builder.url(replicationServer);
        return builder;
    }

    private String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(StandardCharsets.UTF_8)));
    }

    private int getTimeoutInMillis(TimeSpan connectTimeout) {
        return Math.toIntExact(connectTimeout.getTimeUnit().toMillis(connectTimeout.getTime()));
    }
}
