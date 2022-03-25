package mezz.jei.core.test.util;

import mezz.jei.core.util.FileUtil;
import org.junit.jupiter.api.Test;

public class FileUtilTest {
	@Test
	public void testSanitizationOnValidName() {
		String name = "Test";
		String sanitized = FileUtil.sanitizePathName(name);
		assert name.equals(sanitized);
	}

	@Test
	public void testSanitizationOnInvalidName() {
		String name = "Test:123-456_789";
		String expected = "Test_123-456_789";
		String sanitized = FileUtil.sanitizePathName(name);
		assert expected.equals(sanitized);
	}
}
