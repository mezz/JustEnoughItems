package mezz.jei.test;

import mezz.jei.common.config.legacy.LegacyConfigPaths;
import mezz.jei.library.config.ModIdFormatConfig;
import net.mezzdev.config.api.schema.IConfigSchema;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigFileUtil;
import net.mezzdev.config.file.ConfigManager;
import net.mezzdev.config.schema.ConfigSchemaBuilder;
import net.minecraft.ChatFormatting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModIdFormatConfigMigrationTest {
	@Test
	public void migratesLegacyFormattingTransactionallyThroughMezzConfig(@TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jei");
		Path legacyFile = configDirectory.resolve("jei-mod-id-format.ini");
		Path mezzConfigFile = configDirectory.resolve("client").resolve("jei-mod-id-format.ini");
		Path reloadedConfigFile = configDirectory.resolve("reloaded-jei-mod-id-format.ini");
		Files.createDirectories(configDirectory);
		Files.writeString(legacyFile, """
			[modName]
			modNameFormat = RED, BOLD
			""");

		List<ChatFormatting> expected = List.of(ChatFormatting.RED, ChatFormatting.BOLD);
		UUID profileId = UUID.randomUUID();

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JEI Mod Name Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jei", mezzConfigFile, "jei.config.modIdFormat", configManager);
		new ModIdFormatConfig(
			schemaBuilder,
			LegacyConfigPaths.get(configDirectory, profileId, "jei-mod-id-format.ini")
		);
		IConfigSchema schema = schemaBuilder.build();

		assertEquals(expected, getConfiguredValue(schema));
		assertTrue(Files.exists(legacyFile));
		assertTrue(Files.exists(mezzConfigFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		Files.copy(mezzConfigFile, reloadedConfigFile, StandardCopyOption.REPLACE_EXISTING);

		ConfigManager reloadedConfigManager = new ConfigManager("Reloaded JEI Mod Name Config", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder reloadedSchemaBuilder = new ConfigSchemaBuilder("jei", reloadedConfigFile, "jei.config.modIdFormat", reloadedConfigManager);
		new ModIdFormatConfig(reloadedSchemaBuilder);
		IConfigSchema reloadedSchema = reloadedSchemaBuilder.build();
		assertEquals(expected, getConfiguredValue(reloadedSchema));
	}

	private static Object getConfiguredValue(IConfigSchema schema) {
		return schema.getCategories().getFirst().getConfigValues().getFirst().get();
	}
}
