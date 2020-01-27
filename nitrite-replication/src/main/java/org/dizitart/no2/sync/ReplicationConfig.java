package org.dizitart.no2.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import okhttp3.Request;
import org.dizitart.no2.collection.NitriteCollection;

import java.net.Proxy;

/**
 * @author Anindya Chatterjee
 */
@Data
public class ReplicationConfig {
    private NitriteCollection collection;
    private Integer chunkSize;
    private String userName;
    private Integer debounce;
    private ObjectMapper objectMapper;
    private TimeSpan connectTimeout;
    private Request.Builder requestBuilder;
    private Proxy proxy;
    private boolean acceptAllCertificates;
}
