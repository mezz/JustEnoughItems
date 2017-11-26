package mezz.jei.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility to help read and write files in a crash-safe way using a temp file and a backup.
 * If the game crashes while saving, the data saved by this utility should automatically recover.
 */
public final class FileUtil {
	private FileUtil() {
	}

	@FunctionalInterface
	public interface FileOperation {
		void handle(File file) throws IOException;
	}

	@FunctionalInterface
	public interface ZipInputFileOperation {
		void handle(ZipInputStream zipInputStream);
	}

	@FunctionalInterface
	public interface ZipOutputFileOperation {
		void handle(ZipOutputStream zipOutputStream);
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
				Log.get().error("Could not delete old backup file {}", fileBackup.getAbsoluteFile());
			}

			return true;
		} catch (IOException e) {
			Log.get().error("Failed to save file {}.", fileNew, e);
			if (fileBackup.exists()) {
				if (!fileNew.exists() || fileNew.delete()) {
					if (fileBackup.renameTo(fileNew)) {
						Log.get().info("Restored file from backup.");
						return false;
					}
				}
				Log.get().error("Failed to restore file from backup file {}", fileBackup.getAbsoluteFile());
			}
			return false;
		}
	}

	public static void readFileSafely(final File file, final FileOperation fileOperation) {
		if (!file.exists()) {
			final File fileBackup = new File(file.getAbsolutePath() + ".bak");
			if (fileBackup.exists()) {
				if (fileBackup.renameTo(file)) {
					Log.get().info("Restored file from backup.");
				}
			}
		}

		if (file.exists()) {
			try {
				fileOperation.handle(file);
			} catch (IOException e) {
				Log.get().error("Failed to read file {}.", file, e);
			}
		}
	}

	public static void readZipFileSafely(final File file, final String zipEntryName, final ZipInputFileOperation fileOperation) {
		FileUtil.readFileSafely(file, file1 -> {
			final ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file1));
			if (FileUtil.getZipEntry(zipInput, zipEntryName)) {
				fileOperation.handle(zipInput);
				zipInput.close();
			}
		});
	}

	public static boolean writeZipFileSafely(final File file, final String zipEntryName, final ZipOutputFileOperation fileOperation) {
		return FileUtil.writeFileSafely(file, file1 -> {
			ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(file1));
			zipOutput.putNextEntry(new ZipEntry(zipEntryName));
			fileOperation.handle(zipOutput);
			zipOutput.closeEntry();
			zipOutput.close();
		});
	}

	public static boolean getZipEntry(ZipInputStream zipInputStream, String zipEntryName) throws IOException {
		while (true) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			if (zipEntry != null) {
				if (zipEntry.getName().equals(zipEntryName)) {
					return true;
				}
			} else {
				return false;
			}
		}
	}
}
