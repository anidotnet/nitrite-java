package org.dizitart.no2.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import okhttp3.Request;
import org.dizitart.no2.collection.NitriteCollection;

import java.net.Proxy;
import java.util.concurrent.Callable;

/**
 * @author Anindya Chatterjee
 */
@Data
public class Config {
    private NitriteCollection collection;
    private Integer chunkSize;
    private String userName;
    private Integer debounce;
    private ObjectMapper objectMapper;
    private TimeSpan timeout;
    private Request.Builder requestBuilder;
    private Proxy proxy;
    private String authToken;
    private boolean acceptAllCertificates;
    private Callable<Boolean> networkConnectivityChecker;
}
