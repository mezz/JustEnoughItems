package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigFile;

public interface IConfigSchema extends IJeiConfigFile {
    void register(FileWatcher fileWatcher);

    void loadIfNeeded();

    void markDirty();
}
