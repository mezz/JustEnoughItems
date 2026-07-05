package mezz.jei.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class PathUtil {
	private static boolean atomicMoveSupported = true;

	private PathUtil() {
	}

	public static Path writeUsingTempFile(Path path, Iterable<? extends CharSequence> lines) throws IOException {
		Path tempFile = Files.createTempFile(path.getParent(), null, null);
		try {
			Files.write(tempFile, lines);
			return moveAtomicReplace(tempFile, path);
		} finally {
			Files.deleteIfExists(tempFile);
		}
	}

	private static Path moveAtomicReplace(Path source, Path target) throws IOException {
		if (atomicMoveSupported) {
			try {
				return Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ignored) {
				atomicMoveSupported = false;
			}
		}
		return Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
	}
}
