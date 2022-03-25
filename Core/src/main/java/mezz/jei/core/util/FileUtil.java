package mezz.jei.core.util;

public class FileUtil {
    private static final String unsafeFileChars = "[^\\w-]";

    public static String sanitizePathName(String filename) {
        return String.join("_", filename.split(unsafeFileChars));
    }
}
