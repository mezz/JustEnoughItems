package mezz.jei.core.util;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PathUtil {
    private static final String unsafeFileChars = "[^\\w-]";
    private static boolean atomicMoveSupported = true;

    public static String sanitizePathName(String filename) {
        return String.join("_", filename.split(unsafeFileChars));
    }

    public static void writeUsingTempFile(Path path, Iterable<? extends CharSequence> lines) throws IOException {
        Files.createDirectories(path.getParent());
        Path tempFile = Files.createTempFile(path.getParent(), null, null);
        try {
            Files.write(tempFile, lines);
            moveAtomicReplace(tempFile, path);
        } finally {
            if (Files.exists(tempFile)) {
                Files.delete(tempFile);
            }
        }
    }

    private static void moveAtomicReplace(Path source, Path target) throws IOException {
        if (atomicMoveSupported) {
            try {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (AtomicMoveNotSupportedException ignored) {
                atomicMoveSupported = false;
            }
        }
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
