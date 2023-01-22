package mezz.jei.common.config.file;

import mezz.jei.api.runtime.config.IJeiConfigFile;

public interface IConfigSchema extends IJeiConfigFile {
    void register();

    void loadIfNeeded();

    void markDirty();
}
