package mezz.jei.test.lib;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Owns a temporary directory and deletes it when closed.
 */
public final class TemporaryDirectory implements AutoCloseable {
	private final Path temporaryDirectory;

	private TemporaryDirectory(Path temporaryDirectory) {
		this.temporaryDirectory = temporaryDirectory;
	}

	public static TemporaryDirectory create(String name) {
		try {
			Path temporaryDirectory = Files.createTempDirectory("jei-" + name + "-");
			return new TemporaryDirectory(temporaryDirectory);
		} catch (IOException e) {
			throw new AssertionError("Failed to create temporary directory for " + name, e);
		}
	}

	public Path path() {
		return temporaryDirectory;
	}

	@Override
	public void close() {
		try {
			deleteDirectory(temporaryDirectory);
		} catch (IOException e) {
			throw new AssertionError("Failed to delete temporary directory: " + temporaryDirectory, e);
		}
	}

	private static void deleteDirectory(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			return;
		}

		Files.walkFileTree(directory, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc != null) {
					throw exc;
				}
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
