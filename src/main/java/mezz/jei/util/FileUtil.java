package mezz.jei.util;

import java.io.File;
import java.io.IOException;

/**
 * Utility to help read and write files in a crash-safe way using a temp file and a backup.
 * If the game crashes while saving, the data saved by this utility should automatically recover.
 */
public class FileUtil {
	public interface FileOperation {
		void handle(File file) throws IOException;
	}

	public static boolean writeFileSafely(final File file, final FileOperation fileOperation) {
		final File fileNew = new File(file.getAbsolutePath());
		final File fileBackup = new File(file.getAbsolutePath() + ".bak");
		final File fileTemp = new File(file.getAbsolutePath() + ".tmp");

		try {
			if (fileTemp.exists() && !fileTemp.delete()) {
				throw new IOException("Could not delete old temp file");
			}

			fileOperation.handle(fileTemp);

			if (file.exists()) {
				if (fileBackup.exists() && !fileBackup.delete()) {
					throw new IOException("Could not delete old backup");
				}
				if (!file.renameTo(fileBackup)) {
					throw new IOException("Could not backup file");
				}
			}

			if (!fileTemp.renameTo(fileNew)) {
				throw new IOException("Could not rename temp file");
			}

			if (fileBackup.exists() && !fileBackup.delete()) {
				Log.error("Could not delete old backup file {}", fileBackup.getAbsoluteFile());
			}

			return true;
		} catch (IOException e) {
			Log.error("Failed to save file {}.", fileNew, e);
			if (fileBackup.exists()) {
				if (!fileNew.exists() || fileNew.delete()) {
					if (fileBackup.renameTo(fileNew)) {
						Log.info("Restored file from backup.");
						return false;
					}
				}
				Log.error("Failed to restore file from backup file {}", fileBackup.getAbsoluteFile());
			}
			return false;
		}
	}

	public static void readFileSafely(final File file, final FileOperation fileOperation) {
		if (!file.exists()) {
			final File fileBackup = new File(file.getAbsolutePath() + ".bak");
			if (fileBackup.exists()) {
				if (fileBackup.renameTo(file)) {
					Log.info("Restored file from backup.");
				}
			}
		}

		if (file.exists()) {
			try {
				fileOperation.handle(file);
			} catch (IOException e) {
				Log.error("Failed to read file {}.", file, e);
			}
		}
	}
}
