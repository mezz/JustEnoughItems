package mezz.jei.test;

import mezz.jei.common.util.PathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PathUtilTest {
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
}
