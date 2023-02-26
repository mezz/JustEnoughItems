package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigFile;
import mezz.jei.common.config.ConfigManager;

public interface IConfigSchema extends IJeiConfigFile {
    void register(FileWatcher fileWatcher, ConfigManager configManager);

    void loadIfNeeded();

    void markDirty();
}
