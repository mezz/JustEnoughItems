package mezz.jei.common.config.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class FileWatcher {
    private static final Logger LOGGER = LogManager.getLogger();

    private @Nullable FileWatcherThread thread;

    public FileWatcher(String threadName) {
        this.thread = createThread(threadName);
    }

    @Nullable
    private static FileWatcherThread createThread(String threadName) {
        try {
            return new FileWatcherThread(threadName);
        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error("Unable to create file watcher: ", e);
            return null;
        }
    }

    /**
     * @param path     a config file to watch
     * @param callback a callbacks to call when the file changes.
     *                 Callbacks must be thread-safe, they will be called from this thread.
     */
    public void addCallback(Path path, Runnable callback) {
        if (thread != null) {
            this.thread.addCallback(path, callback);
        }
    }

    /**
     * Start the file watcher thread
     */
    public void start() {
        if (thread != null) {
            this.thread.start();
        }
    }

    /**
     * Stop the file watcher thread and clear all callbacks.
     */
    public void reset() {
        if (this.thread == null) {
            return;
        }

        String threadName = this.thread.getName();
        this.thread.interrupt();
        try {
            this.thread.join(1000);
        } catch (InterruptedException consumed) {
            Thread.currentThread().interrupt();
        }

        if (this.thread.isAlive()) {
            LOGGER.error("File Watcher thread could not be stopped and will be abandoned.");
        }

        this.thread = createThread(threadName);
    }
}
