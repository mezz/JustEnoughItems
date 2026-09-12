package mezz.jei.test;

import mezz.jei.library.config.ColorNameConfig;
import mezz.jei.common.config.legacy.LegacyConfigPaths;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigFileUtil;
import net.mezzdev.config.file.ConfigManager;
import net.mezzdev.config.schema.ConfigSchemaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ColorNameConfigMigrationTest {
	@Test
	public void migratesLegacyColorsTransactionallyThroughMezzConfig(@TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jei");
		UUID profileId = UUID.randomUUID();
		Path rootLegacyFile = configDirectory.resolve("jei-colors.ini");
		Path legacyFile = configDirectory.resolve("players").resolve(profileId.toString()).resolve("jei-colors.ini");
		Path mezzConfigFile = configDirectory.resolve("client").resolve("jei-colors.ini");
		Path reloadedConfigFile = configDirectory.resolve("reloaded-jei-colors.ini");
		Files.createDirectories(legacyFile.getParent());
		Files.writeString(rootLegacyFile, """
			[colors]
			searchColors = Root:abcdef
			""");
		Files.writeString(legacyFile, """
			[colors]
			searchColors = Exact:123456, Other:654321
			""");

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JEI Color Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jei", mezzConfigFile, "jei.config.colors", configManager);
		ColorNameConfig colorNameConfig = new ColorNameConfig(
			schemaBuilder,
			LegacyConfigPaths.get(configDirectory, profileId, "jei-colors.ini")
		);
		schemaBuilder.build();

		assertEquals("Exact", colorNameConfig.getClosestColorName(0x123456));
		assertTrue(Files.exists(legacyFile));
		assertTrue(Files.exists(mezzConfigFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		assertTrue(Files.notExists(ConfigFileUtil.getBackupPath(rootLegacyFile, 1)));
		Files.copy(mezzConfigFile, reloadedConfigFile, StandardCopyOption.REPLACE_EXISTING);

		ConfigManager reloadedConfigManager = new ConfigManager("Reloaded JEI Color Config", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder reloadedSchemaBuilder = new ConfigSchemaBuilder("jei", reloadedConfigFile, "jei.config.colors", reloadedConfigManager);
		ColorNameConfig reloaded = new ColorNameConfig(reloadedSchemaBuilder);
		reloadedSchemaBuilder.build();
		assertEquals("Exact", reloaded.getClosestColorName(0x123456));
	}

	@Test
	public void invalidLegacyColorsLeaveTheDestinationAbsent(@TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jei");
		Path legacyFile = configDirectory.resolve("jei-colors.ini");
		Path mezzConfigFile = configDirectory.resolve("client").resolve("jei-colors.ini");
		Files.createDirectories(configDirectory);
		Files.writeString(legacyFile, """
			[colors]
			searchColors = Invalid:not-a-color
			""");

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("Invalid JEI Color Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jei", mezzConfigFile, "jei.config.colors", configManager);
		ColorNameConfig colorNameConfig = new ColorNameConfig(
			schemaBuilder,
			LegacyConfigPaths.get(configDirectory, UUID.randomUUID(), "jei-colors.ini")
		);

		schemaBuilder.build();

		assertEquals("White", colorNameConfig.getClosestColorName(0xEEEEEE));
		assertTrue(Files.notExists(mezzConfigFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
	}
}
