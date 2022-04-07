package mezz.jei.test.util;

import mezz.jei.core.util.PathUtil;
import org.junit.jupiter.api.Test;

public class PathUtilTest {
	@Test
	public void testSanitizationOnValidName() {
		String name = "Test";
		String sanitized = PathUtil.sanitizePathName(name);
		assert name.equals(sanitized);
	}

	@Test
	public void testSanitizationOnInvalidName() {
		String name = "Test:123-456_789";
		String expected = "Test_123-456_789";
		String sanitized = PathUtil.sanitizePathName(name);
		assert expected.equals(sanitized);
	}
}
