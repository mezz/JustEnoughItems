package mezz.jei.common.config.file;

public interface IConfigSchema {
    void loadIfNeeded();

    void register();
}
