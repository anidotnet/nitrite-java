package org.dizitart.no2.store;


import lombok.AccessLevel;
import lombok.Getter;
import org.dizitart.no2.exceptions.InvalidOperationException;
import org.h2.mvstore.FileStore;

import java.io.File;

import static org.dizitart.no2.exceptions.ErrorCodes.IOE_DATABASE_ALREADY_INITIALIZED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;

/**
 * @author Anindya Chatterjee
 */
@Getter
public class MVStoreConfig implements StoreConfig {
    private String filePath;
    private int autoCommitBufferSize;
    private boolean readOnly;
    private boolean compressed;
    private boolean autoCommit = true;
    private boolean autoCompact = true;
    private FileStore fileStore;

    @Getter(AccessLevel.NONE)
    private boolean configured = false;

    /**
     * Sets file name for the file based store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param path the name of the file store.
     * @return the {@link MVStoreConfig} instance.
     */
    public MVStoreConfig filePath(String path) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change the path after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.filePath = path;
        return this;
    }

    /**
     * Sets file name for the file based store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param file the name of the file store.
     * @return the {@link MVStoreConfig} instance.
     */
    public MVStoreConfig filePath(File file) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change the file path after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        if (file == null) {
            this.filePath = null;
        } else {
            this.filePath = file.getPath();
        }
        return this;
    }

    /**
     * Sets {@link FileStore} for mv store. If `file` is `null`
     * the builder will create an in-memory database.
     *
     * @param fileStore the {@link FileStore} instance.
     * @return the {@link MVStoreConfig} instance.
     */
    public MVStoreConfig fileStore(FileStore fileStore) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change the file store after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.fileStore = fileStore;
        return this;
    }

    /**
     * Sets the size of the write buffer, in KB disk space (for file-based
     * stores). Unless auto-commit is disabled, changes are automatically
     * saved if there are more than this amount of changes.
     *
     * When the values is set to 0 or lower, it will assume the default value
     * - 1024 KB.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If auto commit is disabled by {@link MVStoreConfig#disableAutoCommit()},
     * then buffer size has not effect.
     *
     * @param size the buffer size in KB
     * @return the {@link MVStoreConfig} instance.
     */
    public MVStoreConfig autoCommitBufferSize(int size) {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change buffer size after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.autoCommitBufferSize = size;
        return this;
    }

    /**
     * Opens the file in read-only mode. In this case, a shared lock will be
     * acquired to ensure the file is not concurrently opened in write mode.
     *
     * If this option is not used, the file is locked exclusively.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: A file store may only be opened once in every JVM (no matter
     * whether it is opened in read-only or read-write mode), because each
     * file may be locked only once in a process.
     *
     * @return the {@link MVStoreConfig} instance.
     */
    public MVStoreConfig readOnly() {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change readonly property after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.readOnly = true;
        return this;
    }

    /**
     * Compresses data before writing using the LZF algorithm. This will save
     * about 50% of the disk space, but will slow down read and write
     * operations slightly.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: This setting only affects writes; it is not necessary to enable
     * compression when reading, even if compression was enabled when
     * writing.
     *
     * @return the {@link MVStoreConfig} instance.
     */
    public MVStoreConfig compressed() {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change compression property after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.compressed = true;
        return this;
    }

    /**
     * Disables auto commit. If disabled, unsaved changes will not be written
     * into disk until {@link org.dizitart.no2.Nitrite#commit()} is called.
     *
     * By default auto commit is enabled.
     *
     * @return the {@link MVStoreConfig} instance.
     */
    public MVStoreConfig disableAutoCommit() {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change the auto commit property after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.autoCommit = false;
        return this;
    }

    /**
     * Disables auto compact before close. If disabled, compaction will not
     * be performed. Disabling would increase close performance.
     *
     * By default auto compact is enabled.
     *
     * @return the {@link MVStoreConfig} instance.
     */
    public MVStoreConfig disableAutoCompact() {
        if (configured) {
            throw new InvalidOperationException(errorMessage("cannot change auto compact property after database" +
                " initialization", IOE_DATABASE_ALREADY_INITIALIZED));
        }
        this.autoCompact = false;
        return this;
    }

    void configured() {
        this.configured = true;
    }
}
