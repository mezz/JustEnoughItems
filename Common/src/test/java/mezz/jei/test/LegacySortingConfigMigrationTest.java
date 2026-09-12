package mezz.jei.test;

import mezz.jei.common.config.legacy.LegacySortingConfigMigrator;
import net.mezzdev.config.api.sorting.ISortingConfig;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigFileUtil;
import net.mezzdev.config.file.ConfigManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LegacySortingConfigMigrationTest {
	@Test
	public void migratesLegacyValuesTransactionallyThroughMezzConfig(@TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jei");
		UUID profileId = UUID.randomUUID();
		String fileName = "recipe-category-sort-order.ini";
		Path rootLegacyFile = configDirectory.resolve(fileName);
		Path profileLegacyFile = configDirectory.resolve("players").resolve(profileId.toString()).resolve(fileName);
		Path mezzConfigFile = configDirectory.resolve("client").resolve(fileName);
		Files.createDirectories(profileLegacyFile.getParent());
		Files.write(rootLegacyFile, List.of("first", "second"));
		Files.write(profileLegacyFile, List.of("third", "first", "third", "missing"));

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JEI Sorting Config Migration Test", disabledWatcher, disabledWatcher);
		ISortingConfig<String> sortingConfig = configManager.createSortingConfig(mezzConfigFile, Comparator.naturalOrder(), true);
		LegacySortingConfigMigrator.register(
			sortingConfig,
			configDirectory,
			profileId,
			fileName
		);

		List<String> allValues = List.of("first", "second", "third", "fourth");
		List<String> expected = List.of("third", "first", "fourth", "second");
		assertEquals(expected, sortingConfig.getSortedValues(allValues));
		assertTrue(Files.exists(rootLegacyFile));
		assertTrue(Files.exists(profileLegacyFile));
		assertTrue(Files.exists(mezzConfigFile));
		assertEquals(Files.readString(profileLegacyFile), Files.readString(ConfigFileUtil.getBackupPath(profileLegacyFile, 1)));
		assertTrue(Files.notExists(ConfigFileUtil.getBackupPath(rootLegacyFile, 1)));

		ConfigManager reloadedConfigManager = new ConfigManager("Reloaded JEI Sorting Config", disabledWatcher, disabledWatcher);
		ISortingConfig<String> reloaded = reloadedConfigManager.createSortingConfig(mezzConfigFile, Comparator.naturalOrder(), true);
		assertEquals(expected, reloaded.getSortedValues(allValues));
	}
}
