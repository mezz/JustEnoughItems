package mezz.jei.common.config.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.concurrent.ThreadSafe;
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ThreadSafe
public class FileWatcherThread extends Thread {
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * To avoid calling the callbacks many times while a file is being edited,
	 * wait a little while for there to be no more changes before we call them.
	 */
	private static final int quietTimeMs = 500;
	/**
	 * If a directory we want to watch does not exist, we should periodically check for it.
	 */
	private static final int recheckDirectoriesMs = 60_000;

	private final WatchService watchService;
	private final Map<Path, Runnable> callbacks;
	private final Set<Path> directoriesToWatch;

	private final Map<WatchKey, Path> watchedDirectories = new HashMap<>();
	private final Set<Path> changedPaths = new HashSet<>();
	private long nextDirectoryCheckTime = System.currentTimeMillis();

	/**
	 * @param name the name of the new thread
	 */
	public FileWatcherThread(String name) throws IOException {
		super(name);
		this.setDaemon(true);
		this.callbacks = new HashMap<>();
		this.directoriesToWatch = new HashSet<>();
		FileSystem fileSystem = FileSystems.getDefault();
		this.watchService = fileSystem.newWatchService();
	}

	/**
	 * @param path     a config file to watch
	 * @param callback a callbacks to call when the file changes.
	 *                 Callbacks must be thread-safe, they will be called from this thread.
	 */
	public synchronized void addCallback(Path path, Runnable callback) {
		this.callbacks.put(path, callback);
		if (this.directoriesToWatch.add(path.getParent())) {
			this.nextDirectoryCheckTime = System.currentTimeMillis();
		}
	}

	@Override
	public void run() {
		try (watchService) {
			while (!Thread.currentThread().isInterrupted()) {
				runIteration();
			}
		} catch (InterruptedException consumed) {
			LOGGER.info("FileWatcher was interrupted, stopping.");
		} catch (IOException e) {
			LOGGER.error("FileWatcher encountered an unhandled IOException, stopping.", e);
		} finally {
			watchedDirectories
				.keySet()
				.forEach(WatchKey::cancel);
		}
	}

	private void runIteration() throws InterruptedException {
		long time = System.currentTimeMillis();
		if (time > nextDirectoryCheckTime) {
			nextDirectoryCheckTime = time + recheckDirectoriesMs;
			watchDirectories();
		}

		// Collect as many changes as we can, and notify the callbacks when we stop getting new changes.
		WatchKey watchKey = watchService.poll(quietTimeMs, TimeUnit.MILLISECONDS);
		if (watchKey != null) {
			pollWatchKey(watchKey);
		} else {
			notifyChanges();
		}
	}

	private synchronized void pollWatchKey(WatchKey watchKey) throws InterruptedException {
		Path watchedDirectory = watchedDirectories.get(watchKey);
		if (watchedDirectory == null) {
			return;
		}

		List<WatchEvent<?>> events = watchKey.pollEvents();
		for (WatchEvent<?> event : events) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
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

	private synchronized void notifyChanges() {
		if (changedPaths.isEmpty()) {
			return;
		}
		LOGGER.debug("Detected changes in files:\n{}", changedPaths.stream().map(Path::toString).collect(Collectors.joining("\n")));

		List<Runnable> runnables = changedPaths.stream()
			.map(callbacks::get)
			.filter(Objects::nonNull)
			.toList();

		changedPaths.clear();

		// The FileWatcherThread is a daemon thread, so it can stop suddenly
		// when the JVM exits.
		// Because they read and write from the disk, we must run the callbacks
		// in a separate, non-daemon thread so that it prevents the JVM from exiting.
		// This ensures that they can finish running completely, without corrupting data.
		Thread runThread = new CallbackRunner(runnables);
		runThread.start();
	}

	private synchronized void watchDirectories() {
		for (Path directory : directoriesToWatch) {
			if (Thread.currentThread().isInterrupted()) {
				return;
			}
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

	private static class CallbackRunner extends Thread {
		private final List<Runnable> runnables;

		public CallbackRunner(List<Runnable> runnables) {
			super("JEI File Watcher Callback Runner");
			this.runnables = List.copyOf(runnables);
		}

		@Override
		public void run() {
			runnables.forEach(Runnable::run);
		}
	}
}
