package mezz.jei.test;

import mezz.jei.api.runtime.config.IJeiConfigFile;
import mezz.jei.common.config.ConfigManagerAdapter;
import net.mezzdev.config.api.schema.ConfigSchemaType;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigManager;
import net.mezzdev.config.schema.ConfigSchemaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
public class ConfigManagerAdapterTest {
	@Test
	public void legacyEditorCanListJeiFilesWithInactiveAndForeignSchemas(@TempDir Path tempDir) {
		var disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		var configManager = new ConfigManager("JEI Legacy Config Adapter Test", disabledWatcher, disabledWatcher);
		Path clientPath = tempDir.resolve("jei-client.ini");
		var client = new ConfigSchemaBuilder("jei", clientPath, "jei.config.client", configManager);
		client.addCategory("client").addBoolean("enabled", true).build();
		var clientSchema = client.build();

		var otherMod = new ConfigSchemaBuilder("other_mod", tempDir.resolve("other-mod.ini"), "other.config", configManager);
		otherMod.addCategory("client").addBoolean("enabled", true).build();
		var otherModSchema = otherMod.build();

		var inactiveClient = new ConfigSchemaBuilder(
			"jei",
			Optional::empty,
			"jei.config.client",
			configManager,
			ConfigSchemaType.CLIENT,
			null,
			false
		);
		inactiveClient.addCategory("client").addBoolean("enabled", true).build();
		var inactiveSchema = inactiveClient.build();
		assertTrue(inactiveSchema.getPath().isEmpty());

		var legacyManager = ConfigManagerAdapter.create(() -> List.of(clientSchema, otherModSchema, inactiveSchema));
		assertEquals(List.of(clientPath), legacyManager.getConfigFiles().stream().map(IJeiConfigFile::getPath).toList());
	}
}
