package org.dizitart.no2.sync;

import lombok.Data;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.sync.connection.ConnectionConfig;

/**
 * @author Anindya Chatterjee
 */
@Data
public class ReplicationConfig {
    private NitriteCollection collection;
    private ConnectionConfig connectionConfig;
    private Integer chunkSize;
    private String userName;
    private Integer debounce;
}
