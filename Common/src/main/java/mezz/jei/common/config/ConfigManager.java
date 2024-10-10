package mezz.jei.common.config;

import mezz.jei.api.runtime.config.IJeiConfigFile;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import mezz.jei.common.config.file.ConfigSchema;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager implements IJeiConfigManager {
	private final Map<Path, ConfigSchema> configFiles = new HashMap<>();

	public ConfigManager() {

	}

	public void registerConfigFile(ConfigSchema configFile) {
		this.configFiles.put(configFile.getPath(), configFile);
	}

	@Override
	public @Unmodifiable Collection<IJeiConfigFile> getConfigFiles() {
		return Collections.unmodifiableCollection(configFiles.values());
	}

	public void onJeiStarted() {
		configFiles.values().forEach(ConfigSchema::markDirty);
	}
}
