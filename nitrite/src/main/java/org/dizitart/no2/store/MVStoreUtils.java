package org.dizitart.no2.store;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.h2.mvstore.MVStore;

import java.io.File;

import static org.dizitart.no2.common.util.StringUtils.isNullOrEmpty;
import static org.dizitart.no2.exceptions.ErrorCodes.NIOE_DIR_DOES_NOT_EXISTS;
import static org.dizitart.no2.exceptions.ErrorCodes.NIOE_PATH_IS_DIRECTORY;
import static org.dizitart.no2.exceptions.ErrorMessage.*;
import static org.dizitart.no2.store.Security.createSecurely;
import static org.dizitart.no2.store.Security.openSecurely;

/**
 * @author Anindya Chatterjee.
 */
@Slf4j
@UtilityClass
class MVStoreUtils {
    static MVStore openOrCreate(String username, String password, MVStoreConfig mvStoreConfig) {
        try {
            MVStore.Builder builder = new MVStore.Builder();

            if (!isNullOrEmpty(mvStoreConfig.getFilePath())) {
                builder = builder.fileName(mvStoreConfig.getFilePath());
            }

            if (mvStoreConfig.getAutoCommitBufferSize() > 0) {
                builder = builder.autoCommitBufferSize(mvStoreConfig.getAutoCommitBufferSize());
            }

            if (mvStoreConfig.isReadOnly()) {
                if (isNullOrEmpty(mvStoreConfig.getFilePath())) {
                    throw new InvalidOperationException(UNABLE_TO_CREATE_IN_MEMORY_READONLY_DB);
                }
                builder = builder.readOnly();
            }

            if (mvStoreConfig.isCompressed()) {
                builder = builder.compress();
            }

            if (isNullOrEmpty(mvStoreConfig.getFilePath())) {
                // for in-memory store use off-heap storage
                builder = builder.fileStore(mvStoreConfig.getFileStore());
            }

            // auto compact disabled github issue #41
            builder.autoCompactFillRate(0);

            MVStore store = null;
            File dbFile = null;
            try {
                if (!isNullOrEmpty(mvStoreConfig.getFilePath())) {
                    dbFile = new File(mvStoreConfig.getFilePath());
                    if (dbFile.exists()) {
                        store = openSecurely(builder, username, password);
                    } else {
                        store = createSecurely(builder, username, password);
                    }
                } else {
                    store = createSecurely(builder, username, password);
                }
            } catch (IllegalStateException ise) {
                if (ise.getMessage().contains("file is locked")) {
                    throw new NitriteIOException(DATABASE_OPENED_IN_OTHER_PROCESS);
                }

                if (!isNullOrEmpty(mvStoreConfig.getFilePath())) {
                    try {
                        File file = new File(mvStoreConfig.getFilePath());
                        if (file.isDirectory()) {
                            throw new NitriteIOException(errorMessage(mvStoreConfig.getFilePath()
                                + " is a directory, must be a file", NIOE_PATH_IS_DIRECTORY));
                        }

                        if (file.exists() && file.isFile()) {
                            log.error("Database corruption detected.", ise);
                            throw new NitriteIOException(DB_FILE_CORRUPTED, ise);
                        } else {
                            if (mvStoreConfig.isReadOnly()) {
                                throw new NitriteIOException(FAILED_TO_CREATE_READONLY_DB, ise);
                            }
                        }
                    } catch (InvalidOperationException | NitriteIOException ex) {
                        throw ex;
                    } catch (Exception e) {
                        throw new NitriteIOException(DB_FILE_CORRUPTED, e);
                    }
                } else {
                    throw new NitriteIOException(UNABLE_TO_CREATE_IN_MEMORY_DB, ise);
                }
            } catch (IllegalArgumentException iae) {
                if (dbFile != null) {
                    if (!dbFile.getParentFile().exists()) {
                        throw new NitriteIOException(errorMessage("Directory " + dbFile.getParent() + " does not exists",
                            NIOE_DIR_DOES_NOT_EXISTS), iae);
                    }
                }
                throw new NitriteIOException(UNABLE_TO_CREATE_DB_FILE, iae);
            } finally {
                if (store != null) {
                    store.setRetentionTime(-1);
                    store.setVersionsToKeep(2);
                    store.setReuseSpace(true);
                }
            }

            return store;
        } finally {
            mvStoreConfig.configured();
        }
    }
}
