package mezz.jei.common.startup;

import mezz.jei.common.config.EditModeConfigInternal;
import mezz.jei.common.config.IModIdFormatConfig;
import mezz.jei.common.config.JeiDebugConfigs;
import mezz.jei.common.config.ModIdFormatConfig;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.common.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record ConfigData(
    EditModeConfigInternal editModeConfig,
    RecipeCategorySortingConfig recipeCategorySortingConfig,
    IModIdFormatConfig modIdFormatConfig
) {
    public static ConfigData create() {
        Path configDir = Services.PLATFORM.getConfigDir();
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create JEI config directory: " + configDir, e);
        }

        JeiDebugConfigs jeiDebugConfigs = new JeiDebugConfigs(configDir.resolve("jei-debug.ini"));
        jeiDebugConfigs.register();

        ModIdFormatConfig modIdFormatConfig = new ModIdFormatConfig(configDir.resolve("jei-mod-id-format.ini"));
        modIdFormatConfig.register();

        EditModeConfigInternal editModeConfig = new EditModeConfigInternal(new EditModeConfigInternal.FileSerializer(configDir.resolve("blacklist.cfg")));
        RecipeCategorySortingConfig recipeCategorySortingConfig = new RecipeCategorySortingConfig(configDir.resolve("recipe-category-sort-order.ini"));

        return new ConfigData(
            editModeConfig,
            recipeCategorySortingConfig,
            modIdFormatConfig
        );
    }
}
