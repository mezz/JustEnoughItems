package mezz.jei.test;

import mezz.jei.common.config.ClientConfigs;
import mezz.jei.common.config.WorldConfig;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigManager;
import net.mezzdev.config.schema.ConfigSchemaBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OverlayPersistenceTest {
	@Test
	public void hidingOverlayPersistsAcrossConfigReloads(@TempDir Path tempDir) throws Exception {
		Path configFile = tempDir.resolve("jei-client.ini");
		try (ConfigSession session = createSession(configFile)) {
			WorldConfig toggles = session.createToggleState();
			assertTrue(toggles.isOverlayEnabled());
			toggles.clearListeners();
			toggles.toggleOverlayEnabled();
			assertFalse(toggles.isOverlayEnabled());
		}
		try (ConfigSession session = createSession(configFile)) {
			WorldConfig reloaded = session.createToggleState();
			assertFalse(reloaded.isOverlayEnabled(), "The hidden overlay must stay hidden after reloading its config");
			reloaded.toggleOverlayEnabled();
		}
		try (ConfigSession session = createSession(configFile)) {
			assertTrue(session.createToggleState().isOverlayEnabled());
		}
	}

	@Test
	public void configEditsAndTogglesShareTheSavedPreference(@TempDir Path tempDir) throws Exception {
		Path configFile = tempDir.resolve("jei-client.ini");
		try (ConfigSession session = createSession(configFile)) {
			ClientConfigs configs = session.configs();
			WorldConfig toggles = session.createToggleState();
			List<Boolean> changes = new ArrayList<>();
			Runnable removeListener = toggles.addOverlayEnabledListener(changes::add);

			configs.getClientConfig().overlaysEnabled().set(false);
			assertFalse(toggles.isOverlayEnabled());
			toggles.toggleOverlayEnabled();
			assertTrue(configs.getClientConfig().overlaysEnabled().get());
			toggles.setOverlayEnabled(true);
			assertEquals(List.of(false, true), changes);

			removeListener.run();
			toggles.clearListeners();
			configs.getClientConfig().overlaysEnabled().set(false);
			assertFalse(toggles.isOverlayEnabled());
			assertEquals(List.of(false, true), changes);
		}
		try (ConfigSession session = createSession(configFile)) {
			assertFalse(session.createToggleState().isOverlayEnabled());
		}
	}

	private static ConfigSession createSession(Path configFile) {
		ConfigFileWatcherSettings disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		ConfigManager manager = new ConfigManager("JEI Overlay Persistence Test", disabledWatcher, disabledWatcher);
		ClientConfigs configs = new ClientConfigs(
			new ConfigSchemaBuilder("jei", configFile, "jei.config.client", manager),
			false,
			manager.createInMemorySortingConfig(Comparator.naturalOrder(), true)
		);
		return new ConfigSession(manager, configs);
	}

	private record ConfigSession(ConfigManager manager, ClientConfigs configs) implements AutoCloseable {
		WorldConfig createToggleState() {
			return new WorldConfig(configs.getClientConfig().overlaysEnabled());
		}

		@Override
		public void close() throws Exception {
			// Flush pending saves through the same executor used by MezzConfig's JVM shutdown hook.
			((AutoCloseable) manager.getSaveScheduler()).close();
		}
	}
}
