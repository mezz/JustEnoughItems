package mezz.jei.common.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class PathUtil {
	private static final Pattern unsafeFileChars = Pattern.compile("[\\\\/.:*?\"<>|]");
	private static final String legacyUnsafeFileChars = "[^\\w-]";
	private static final Pattern reservedWindowsFilenames = Pattern.compile(".*\\.|(?:CON|PRN|AUX|NUL|CLOCK\\$|CONIN\\$|CONOUT\\$|(?:COM|LPT)[¹²³0-9])(?:\\..*)?", Pattern.CASE_INSENSITIVE);
	private static boolean atomicMoveSupported = true;

	public static String sanitizePathName(String filename) {
		String sanitized = unsafeFileChars.matcher(filename)
			.replaceAll("_")
			.trim();
		if (sanitized.isEmpty()) {
			return "_";
		}
		if (reservedWindowsFilenames.matcher(sanitized).matches()) {
			return "_%s_".formatted(sanitized);
		}
		return sanitized;
	}

	public static String sanitizePathNameLegacy(String filename) {
		return String.join("_", filename.split(legacyUnsafeFileChars));
	}

	public static boolean migrateConfigLocation(Path newFile, Path oldFile) throws IOException {
		if (Files.exists(newFile) || !Files.exists(oldFile)) {
			return false;
		}
		moveAtomicReplace(oldFile, newFile);
		return true;
	}

	public static Path writeUsingTempFile(Path path, Iterable<? extends CharSequence> lines) throws IOException {
		Files.createDirectories(path.getParent());
		Path tempFile = Files.createTempFile(path.getParent(), null, null);
		try {
			Files.write(tempFile, lines);
			return moveAtomicReplace(tempFile, path);
		} finally {
			if (Files.exists(tempFile)) {
				Files.delete(tempFile);
			}
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
