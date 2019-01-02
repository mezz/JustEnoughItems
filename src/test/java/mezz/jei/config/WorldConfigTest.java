package mezz.jei.config;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraftforge.common.config.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class WorldConfigTest extends ForgeConfigTestBase {
	@Test
	public void settingsAndSearchTextRemainSeparateAcrossWorldsAndReloads() throws IOException {
		File file = temporaryFolder.newFile("worldSettings.cfg");
		AtomicReference<String> world = new AtomicReference<>("world-a");
		WorldConfig config = new WorldConfig(new Configuration(file, "0.1.0"), world::get);
		config.syncWorldConfigForWorld(world.get());

		config.setFilterText("#cobalt");
		config.saveFilterText();
		config.toggleOverlayEnabled();
		config.toggleBookmarkEnabled();
		config.toggleEditModeEnabled();
		config.setCheatItemsEnabled(true);
		world.set("world-b");
		config.syncWorldConfigForWorld(world.get());
		config.setFilterText("@second");
		config.saveFilterText();
		Configuration saved = new Configuration(file, "0.1.0");
		WorldConfig reloaded = new WorldConfig(saved, world::get);
		reloaded.syncWorldConfigForWorld("world-a");

		Assert.assertEquals("#cobalt", reloaded.getFilterText());
		Assert.assertTrue(reloaded.isEditModeEnabled());
		Assert.assertTrue(reloaded.isCheatItemsEnabled());
		Assert.assertFalse(saved.get("world-a", "overlayEnabled", true).getBoolean());
		Assert.assertFalse(saved.get("world-a", "bookmarkOverlayEnabled", true).getBoolean());

		reloaded.syncWorldConfigForWorld("world-b");

		Assert.assertEquals("@second", reloaded.getFilterText());
		Assert.assertFalse(reloaded.isEditModeEnabled());
		Assert.assertFalse(reloaded.isCheatItemsEnabled());
		Assert.assertTrue(saved.get("world-b", "overlayEnabled", false).getBoolean());
		Assert.assertTrue(saved.get("world-b", "bookmarkOverlayEnabled", false).getBoolean());
	}

	@Test
	public void loadingAnotherConfigurationDoesNotChangeAnExistingWorldContext() throws IOException {
		Configuration firstFile = new Configuration(temporaryFolder.newFile("first.cfg"), "0.1.0");
		Configuration secondFile = new Configuration(temporaryFolder.newFile("second.cfg"), "0.1.0");
		firstFile.get("world", "filterText", "").set("apple");
		secondFile.get("world", "filterText", "").set("pear");
		WorldConfig first = new WorldConfig(firstFile, () -> "world");
		WorldConfig second = new WorldConfig(secondFile, () -> "world");

		first.syncWorldConfigForWorld("world");
		second.syncWorldConfigForWorld("world");
		second.setFilterText("#cobalt");
		second.saveFilterText();

		Assert.assertEquals("apple", first.getFilterText());
		Assert.assertEquals("#cobalt", new Configuration(secondFile.getConfigFile(), "0.1.0").get("world", "filterText", "").getString());
	}

	@Test
	public void legacyStaticAccessorsShareTheWorldStateAndServerPermissionGate() {
		String previousText = Config.getFilterText();
		boolean previousCheatMode = Config.isCheatItemsEnabled();
		boolean previousServer = ServerInfo.isJeiOnServer();
		try {
			ServerInfo.onConnectedToServer(false);
			Config.setFilterText("world query");
			Config.setCheatItemsEnabled(true);

			Assert.assertEquals("world query", Config.getFilterTextSource().getFilterText());
			Assert.assertTrue(Config.isCheatItemsEnabled());
			Assert.assertFalse(Config.isDeleteItemsInCheatModeActive());

			ServerInfo.onConnectedToServer(true);

			Assert.assertTrue(Config.isDeleteItemsInCheatModeActive());
		} finally {
			ServerInfo.onConnectedToServer(false);
			Config.setFilterText(previousText);
			Config.setCheatItemsEnabled(previousCheatMode);
			ServerInfo.onConnectedToServer(previousServer);
		}
	}
}
