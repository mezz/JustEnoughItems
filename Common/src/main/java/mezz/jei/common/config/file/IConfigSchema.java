package mezz.jei.common.config.file;

import java.nio.file.Path;

public interface IConfigSchema {
    void loadIfNeeded();

    Path getPath();

    void register(Path configFile);
}
