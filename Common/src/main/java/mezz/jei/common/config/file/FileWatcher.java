package mezz.jei.common.config.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class FileWatcher {
    private static final Logger LOGGER = LogManager.getLogger();

    private final @Nullable FileWatcherThread thread;

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
            thread.addCallback(path, callback);
        }
    }

    public void start() {
        if (thread != null) {
            thread.start();
        }
    }
}
