package mezz.jei.test;

import mezz.jei.common.util.PathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class PathUtilTest {
	@TempDir
	Path tempDir;

	@Test
	public void testSanitizationPreservesPortableName() {
		// Setup: the path name uses characters that are portable across Windows, macOS, and Linux.
		String name = "Test Server (play_example_com_25565)";

		// Operation
		String sanitized = PathUtil.sanitizePathName(name);

		// Assertions: readable portable characters are preserved.
		Assertions.assertEquals(name, sanitized);
	}

	@Test
	public void testSanitizationReplacesPathSeparators() {
		// Setup: the path name uses path separators.
		String name = "Test/123\\456";

		// Operation
		String sanitized = PathUtil.sanitizePathName(name);

		// Assertions: separators are replaced so the name stays a single path segment.
		Assertions.assertEquals("Test_123_456", sanitized);
	}

	@Test
	public void testSanitizationReplacesWindowsInvalidCharacters() {
		// Setup: the path name uses characters that are invalid on Windows and still awkward on other platforms.
		String name = "Test:123*456?789\"abc<def>ghi|jkl";

		// Operation
		String sanitized = PathUtil.sanitizePathName(name);

		// Assertions: the invalid characters are replaced.
		Assertions.assertEquals("Test_123_456_789_abc_def_ghi_jkl", sanitized);
	}

	@Test
	public void testSanitizationAvoidsReservedWindowsNames() {
		// Setup: the path name is a reserved Windows device name.
		String name = "CON";

		// Operation
		String sanitized = PathUtil.sanitizePathName(name);

		// Assertions: the reserved name is changed to a portable path name.
		Assertions.assertEquals("_CON_", sanitized);
	}

	@Test
	public void testSanitizationAvoidsEmptyNames() {
		// Setup: the path name is empty after trimming.
		String name = " ";

		// Operation
		String sanitized = PathUtil.sanitizePathName(name);

		// Assertions: the path name is still usable as a path segment.
		Assertions.assertEquals("_", sanitized);
	}

	@Test
	public void testLegacySanitizationMatchesOldPathNames() {
		// Setup: the path name uses characters that the old JEI sanitizer replaced.
		String name = "Test Server: 1 (play.example.com:25565)";

		// Operation
		String sanitized = PathUtil.sanitizePathNameLegacy(name);

		// Assertions: the old restrictive replacement behavior is preserved.
		Assertions.assertEquals("Test_Server__1__play_example_com_25565", sanitized);
	}

	@Test
	public void testWriteUsingTempFileReplacesExistingFile() throws IOException {
		// Setup: an existing config file has stale contents.
		Path path = tempDir.resolve("bookmarks.ini");
		Files.write(path, List.of("old contents"));

		// Operation: write a complete replacement through the path helper.
		PathUtil.writeUsingTempFile(path, List.of("new"));

		// Assertions: the target file is replaced, and no temporary files are left behind.
		Assertions.assertEquals(List.of("new"), Files.readAllLines(path));
		try (Stream<Path> files = Files.list(tempDir)) {
			Assertions.assertEquals(List.of(path), files.toList());
		}
	}

	@Test
	public void testWriteUsingTempFileCreatesParentDirectories() throws IOException {
		// Setup: the target config file is inside a world-specific directory that does not exist yet.
		Path path = tempDir.resolve("world").resolve("local").resolve("bookmarks.ini");

		// Operation: write the config through the path helper.
		PathUtil.writeUsingTempFile(path, List.of("new"));

		// Assertions: the helper creates parent directories before writing the file.
		Assertions.assertEquals(List.of("new"), Files.readAllLines(path));
	}

	@Test
	public void testWriteUsingTempFileKeepsExistingFileWhenIterationFails() throws IOException {
		// Setup: an existing config file is present, and the replacement collection fails while being iterated.
		Path path = tempDir.resolve("bookmarks.ini");
		List<String> originalContents = List.of("existing");
		Files.write(path, originalContents);

		// Operation: attempt to write the replacement file.
		Assertions.assertThrows(
			ConcurrentModificationException.class,
			() -> PathUtil.writeUsingTempFile(path, new FailingIterable())
		);

		// Assertions: the original file is intact, and the failed temporary write is cleaned up.
		Assertions.assertEquals(originalContents, Files.readAllLines(path));
		try (Stream<Path> files = Files.list(tempDir)) {
			Assertions.assertEquals(List.of(path), files.toList());
		}
	}

	private static class FailingIterable extends AbstractCollection<String> {
		@Override
		public Iterator<String> iterator() {
			return new Iterator<>() {
				private int index;

				@Override
				public boolean hasNext() {
					return index < 2;
				}

				@Override
				public String next() {
					index++;
					if (index == 2) {
						throw new ConcurrentModificationException();
					}
					return "new";
				}
			};
		}

		@Override
		public int size() {
			return 2;
		}
	}
}
