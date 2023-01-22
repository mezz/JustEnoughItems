package mezz.jei.common.config;

import mezz.jei.api.runtime.config.IJeiConfigFile;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager implements IJeiConfigManager {
    public static final ConfigManager INSTANCE = new ConfigManager();

    private final Map<Path, IJeiConfigFile> configFiles = new HashMap<>();

    private ConfigManager() {

    }

    public void registerConfigFile(IJeiConfigFile configFile) {
        this.configFiles.put(configFile.getPath(), configFile);
    }

    @Override
    public @Unmodifiable Collection<IJeiConfigFile> getConfigFiles() {
        return Collections.unmodifiableCollection(configFiles.values());
    }
}
