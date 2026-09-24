package mezz.jei.test;

import mezz.jei.common.config.legacy.LegacyConfigPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LegacyConfigPathsTest {
	@Test
	public void usesGlobalConfigPathWhenProfileIsUnavailable(@TempDir Path tempDir) {
		Path configDirectory = tempDir.resolve("jei");
		String fileName = "jei-client.ini";

		List<Path> paths = LegacyConfigPaths.get(configDirectory, null, fileName);

		assertEquals(
			List.of(configDirectory.toAbsolutePath().normalize().resolve(fileName)),
			paths
		);
	}
}
