package mezz.jei.common.config.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FileWatcher {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * To avoid calling the callbacks many times while a file is being edited,
     * wait a little while for there to be no more changes before we call them.
     */
    private static final int quietTimeMs = 1_000;
    /**
     * If a directory we want to watch does not exist, we should periodically check for it.
     */
    private static final int recheckDirectoriesMs = 60_000;

    private final WatchService watchService;
    @Unmodifiable
    private final Map<Path, Runnable> callbacks;
    @Unmodifiable
    private final Set<Path> directoriesToWatch;

    private final Map<WatchKey, Path> watchedDirectories = new HashMap<>();
    private final Set<Path> changedPaths = new HashSet<>();
    private long lastRecheckTime = 0;

    /**
     * @param callbacks a map of files to watch and callbacks to call when a file changes
     */
    public FileWatcher(Map<Path, Runnable> callbacks) throws IOException {
        this.callbacks = Map.copyOf(callbacks);
        this.directoriesToWatch = callbacks.keySet().stream()
            .map(Path::getParent)
            .collect(Collectors.toUnmodifiableSet());
        FileSystem fileSystem = FileSystems.getDefault();
        this.watchService = fileSystem.newWatchService();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try (watchService) {
            while (true) {
                runIteration();
            }
        } catch (InterruptedException e) {
            LOGGER.error("FileWatcher was interrupted, stopping.", e);
        } catch (IOException e) {
            LOGGER.error("FileWatcher encountered an unhandled IOException, stopping.", e);
        }
        notifyChanges();
    }

    private void runIteration() throws InterruptedException {
        long time = System.currentTimeMillis();
        if (time > lastRecheckTime + recheckDirectoriesMs) {
            lastRecheckTime = time;
            watchDirectories();
        }

        if (changedPaths.isEmpty()) {
            // There are no changes yet.
            // Just block and wait for some changes.
            WatchKey watchKey = watchService.take();
            if (watchKey != null) {
                pollWatchKey(watchKey);
            }
        } else {
            // We have some detected some changes already.
            // Collect more changes, or notify the callbacks if there are no new changes.
            WatchKey watchKey = watchService.poll(quietTimeMs, TimeUnit.MILLISECONDS);
            if (watchKey != null) {
                pollWatchKey(watchKey);
            } else {
                notifyChanges();
            }
        }
    }

    private void pollWatchKey(WatchKey watchKey) {
        Path watchedDirectory = watchedDirectories.get(watchKey);
        if (watchedDirectory == null) {
            return;
        }

        List<WatchEvent<?>> events = watchKey.pollEvents();
        for (WatchEvent<?> event : events) {
            if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                // we missed some events,
                // so we must assume every watched file in the directory has changed
                callbacks.keySet().stream()
                    .filter(path -> path.getParent().equals(watchedDirectory))
                    .forEach(changedPaths::add);
                break;
            } else if (event.context() instanceof Path eventPath) {
                Path fullPath = watchedDirectory.resolve(eventPath);
                if (callbacks.containsKey(fullPath)) {
                    changedPaths.add(fullPath);
                }
            }
        }

        if (!watchKey.reset()) {
            LOGGER.info("Failed to re-watch directory {}. It may have been deleted.", watchedDirectory);
            watchedDirectories.remove(watchKey);
        }
    }

    private void notifyChanges() {
        if (changedPaths.isEmpty()) {
            return;
        }
        LOGGER.info("Detected changes in files:\n{}", changedPaths.stream().map(Path::toString).collect(Collectors.joining("\n")));
        for (Path changedPath : changedPaths) {
            Runnable runnable = callbacks.get(changedPath);
            if (runnable != null) {
                runnable.run();
            }
        }
        changedPaths.clear();
    }

    private void watchDirectories() {
        for (Path directory : directoriesToWatch) {
            if (!watchedDirectories.containsValue(directory) &&
                Files.isDirectory(directory)
            ) {
                try {
                    WatchKey key = directory.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.OVERFLOW
                    );
                    watchedDirectories.put(key, directory);
                } catch (IOException e) {
                    LOGGER.error("Failed to watch directory: {}", directory, e);
                }
            }
        }
    }
}
