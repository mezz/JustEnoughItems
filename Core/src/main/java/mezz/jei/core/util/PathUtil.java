package mezz.jei.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PathUtil {
    private static final String unsafeFileChars = "[^\\w-]";

    public static String sanitizePathName(String filename) {
        return String.join("_", filename.split(unsafeFileChars));
    }

    public static boolean migrateConfigLocation(Path newFile, Path oldFile) throws IOException {
        if (Files.exists(newFile) || !Files.exists(oldFile)) {
            return false;
        }
        Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }
}
