package mezz.jei.test;

import mezz.jei.common.config.legacy.LegacyServerConfigMigration;
import net.mezzdev.config.api.schema.IConfigSchema;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigManager;
import net.mezzdev.config.schema.ConfigSchemaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerConfigMigrationTest {
	@ParameterizedTest
	@ValueSource(strings = {"jei-server.properties", "jei-server.toml"})
	void importsLegacyPermissionsAndPersistsThemWithoutChangingTheSource(String fileName, @TempDir Path tempDir) throws IOException {
		Path legacy = tempDir.resolve(fileName);
		String contents = """
			"enableCheatModeForOp" = false
			'enableCheatModeForCreative' = invalid
			enableCheatModeForGive = true # allow give permission
			""";
		if (fileName.endsWith(".toml")) {
			contents = "[unrelated]\nenableCheatModeForGive = false\n[\"cheat mode\"]\n" + contents;
		}
		Files.writeString(legacy, contents);
		Path destination = tempDir.resolve("world/serverconfig/jei/jei-server.ini");
		LegacyServerConfigMigration migration = LegacyServerConfigMigration.prepare(destination, List.of(legacy)).orElseThrow();
		Permissions permissions = createPermissions(destination);

		// Import through the real schema writer, then reload the resulting world configuration.
		migration.apply(permissions.schema(), permissions.values());
		assertEquals(List.of(false, true, true), permissions.values().stream().map(IConfigValue::get).toList());
		// MezzConfig batches file saves asynchronously; wait for persistence before reopening the schema.
		assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
			while (!Files.readString(destination).contains("enableCheatModeForOp = false") ||
				!Files.readString(destination).contains("enableCheatModeForGive = true")
			) {
				Thread.sleep(10);
			}
		});
		Permissions reloaded = createPermissions(destination);

		assertEquals(List.of(false, true, true), reloaded.values().stream().map(IConfigValue::get).toList());
		assertEquals(contents, Files.readString(legacy), "Legacy files must remain unchanged after migration");
	}

	@Test
	void preservesExistingWorldPermissions(@TempDir Path tempDir) throws IOException {
		Path legacy = tempDir.resolve("jei-server.properties");
		Files.writeString(legacy, "enableCheatModeForOp = true\nenableCheatModeForGive = true\n");
		Path destination = tempDir.resolve("jei-server.ini");
		String existing = "[permissions]\nenableCheatModeForOp = false\nenableCheatModeForGive = false\n";
		Files.writeString(destination, existing);

		var migration = LegacyServerConfigMigration.prepare(destination, List.of(legacy));
		Permissions permissions = createPermissions(destination);

		assertTrue(migration.isEmpty(), "Existing world settings must prevent an automatic legacy import");
		assertEquals(List.of(false, true, false), permissions.values().stream().map(IConfigValue::get).toList());
		assertEquals(List.of(false, true, false), createPermissions(destination).values().stream().map(IConfigValue::get).toList());
	}

	@Test
	void rejectsImportIntoAnotherWorld(@TempDir Path tempDir) throws IOException {
		Path legacy = tempDir.resolve("jei-server.properties");
		Files.writeString(legacy, "enableCheatModeForOp = false\n");
		Path destination = tempDir.resolve("first-world/jei-server.ini");
		LegacyServerConfigMigration migration = LegacyServerConfigMigration.prepare(destination, List.of(legacy)).orElseThrow();
		Path otherWorld = tempDir.resolve("other-world/jei-server.ini");
		Permissions permissions = createPermissions(otherWorld);

		assertThrows(IllegalStateException.class, () -> migration.apply(permissions.schema(), permissions.values()));

		assertEquals(List.of(true, true, false), permissions.values().stream().map(IConfigValue::get).toList());
	}

	private static Permissions createPermissions(Path path) {
		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager manager = new ConfigManager("JEI Server Config Migration Test", disabledWatcher, disabledWatcher);
		ConfigSchemaBuilder builder = new ConfigSchemaBuilder("jei", path, "jei.config.server", manager);
		var category = builder.addCategory("permissions");
		List<IConfigValue<Boolean>> values = List.of(
			category.addBoolean("enableCheatModeForOp", true).build(),
			category.addBoolean("enableCheatModeForCreative", true).build(),
			category.addBoolean("enableCheatModeForGive", false).build()
		);
		return new Permissions(builder.build(), values);
	}

	private record Permissions(IConfigSchema schema, List<IConfigValue<Boolean>> values) {}
}
