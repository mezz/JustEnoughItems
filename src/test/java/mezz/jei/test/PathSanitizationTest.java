package mezz.jei.test;

import mezz.jei.config.ServerInfo;
import org.junit.jupiter.api.Test;

public class PathSanitizationTest {
	@Test
	public void testSanitizationOnValidName() {
		String name = "Test";
		String sanitized = ServerInfo.sanitizePathName(name);
		assert name.equals(sanitized);
	}

	@Test
	public void testSanitizationOnInvalidName() {
		String name = "Test:123-456_789";
		String expected = "Test_123-456_789";
		String sanitized = ServerInfo.sanitizePathName(name);
		assert expected.equals(sanitized);
	}
}
