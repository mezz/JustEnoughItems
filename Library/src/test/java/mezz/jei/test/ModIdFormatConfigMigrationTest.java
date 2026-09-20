package mezz.jei.test;

import mezz.jei.common.config.legacy.LegacyConfigPaths;
import mezz.jei.library.config.ModIdFormatConfig;
import net.mezzdev.config.api.schema.IConfigSchema;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigFileUtil;
import net.mezzdev.config.file.ConfigManager;
import net.mezzdev.config.schema.ConfigSchemaBuilder;
import net.mezzdev.config.schema.LayeredConfigSchemaPathResolver;
import net.mezzdev.config.schema.StaticConfigSchemaPathResolver;
import net.minecraft.ChatFormatting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModIdFormatConfigMigrationTest {
	private static Stream<Arguments> legacyFormats() {
		return Stream.of(
			Arguments.of("red bold", List.of(ChatFormatting.RED, ChatFormatting.BOLD)),
			Arguments.of("\"red bold\"", List.of(ChatFormatting.RED, ChatFormatting.BOLD)),
			Arguments.of("RED, BOLD", List.of(ChatFormatting.RED, ChatFormatting.BOLD)),
			Arguments.of("", List.of()),
			Arguments.of("\"\"", List.of())
		);
	}

	@ParameterizedTest
	@MethodSource("legacyFormats")
	public void migratesLegacyFormattingTransactionallyThroughMezzConfig(String legacyFormat, List<ChatFormatting> expected, @TempDir Path tempDir) throws IOException {
		Path configDirectory = tempDir.resolve("jei");
		Path legacyFile = configDirectory.resolve("jei-mod-id-format.ini");
		Path mezzConfigFile = configDirectory.resolve("client").resolve("jei-mod-id-format.ini");
		Files.createDirectories(configDirectory);
		Files.writeString(legacyFile, "[modName]\nmodNameFormat = " + legacyFormat + "\n");
		UUID profileId = UUID.randomUUID();

		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager configManager = new ConfigManager("JEI Mod Name Config Migration Test", disabledWatcher, disabledWatcher);
		var pathResolver = new LayeredConfigSchemaPathResolver(
			configDirectory.resolve("client/default/jei-mod-id-format.ini"),
			new StaticConfigSchemaPathResolver(mezzConfigFile)
		);
		ConfigSchemaBuilder schemaBuilder = new ConfigSchemaBuilder("jei", pathResolver, "jei.config.modIdFormat", configManager);
		new ModIdFormatConfig(
			schemaBuilder,
			LegacyConfigPaths.get(configDirectory, profileId, "jei-mod-id-format.ini")
		);
		IConfigSchema schema = schemaBuilder.build();

		assertEquals(expected, getConfiguredValue(schema));
		assertTrue(Files.exists(legacyFile));
		assertTrue(Files.exists(mezzConfigFile));
		assertEquals(Files.readString(legacyFile), Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));

		ConfigManager reloadedConfigManager = new ConfigManager("Reloaded JEI Mod Name Config", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder reloadedSchemaBuilder = new ConfigSchemaBuilder("jei", pathResolver, "jei.config.modIdFormat", reloadedConfigManager);
		new ModIdFormatConfig(reloadedSchemaBuilder);
		IConfigSchema reloadedSchema = reloadedSchemaBuilder.build();
		assertEquals(expected, getConfiguredValue(reloadedSchema));
	}

	@Test
	public void keepsExistingSettingsAndAllowsAnExplicitMigrationRetry(@TempDir Path tempDir) throws IOException {
		Path legacyFile = tempDir.resolve("jei-mod-id-format.ini");
		Path currentFile = tempDir.resolve("client/jei-mod-id-format.ini");
		Files.createDirectories(currentFile.getParent());
		String disabledFormatting = "[modName]\nmodNameFormat =\n";
		Files.writeString(legacyFile, disabledFormatting);
		Files.writeString(currentFile, "[modName]\nmodNameFormat = [\"GREEN\"]\n");

		assertEquals(List.of(ChatFormatting.GREEN), loadWithMigration(currentFile, legacyFile));
		assertTrue(Files.notExists(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		assertEquals(disabledFormatting, Files.readString(legacyFile));

		// A user can preserve the current config and explicitly retry the legacy import.
		Path previousConfig = currentFile.resolveSibling("jei-mod-id-format.ini.before-reimport");
		Files.move(currentFile, previousConfig);
		assertEquals(List.of(), loadWithMigration(currentFile, legacyFile));
		assertTrue(Files.exists(previousConfig));
		assertEquals(disabledFormatting, Files.readString(ConfigFileUtil.getBackupPath(legacyFile, 1)));
		assertEquals(List.of(), loadWithMigration(currentFile, legacyFile));
	}

	private static Object loadWithMigration(Path currentFile, Path legacyFile) {
		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager manager = new ConfigManager("JEI Mod Name Migration Retry Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder builder = new ConfigSchemaBuilder("jei", currentFile, "jei.config.modIdFormat", manager);
		new ModIdFormatConfig(builder, List.of(legacyFile));
		return getConfiguredValue(builder.build());
	}

	private static Object getConfiguredValue(IConfigSchema schema) {
		return schema.getCategories().getFirst().getConfigValues().getFirst().get();
	}
}
