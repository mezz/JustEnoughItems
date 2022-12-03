package mezz.jei.library.startup;

import mezz.jei.library.config.ColorNameConfig;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.EditModeConfigInternal;
import mezz.jei.common.config.IModIdFormatConfig;
import mezz.jei.common.config.ModIdFormatConfig;
import mezz.jei.common.config.file.ConfigSchemaBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.common.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record ConfigData(
    EditModeConfigInternal editModeConfig,
    RecipeCategorySortingConfig recipeCategorySortingConfig,
    IModIdFormatConfig modIdFormatConfig,
    ColorNameConfig colorNameConfig
) {
    public static ConfigData create() {
        Path configDir = Services.PLATFORM.getConfigDir();
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create JEI config directory: " + configDir, e);
        }

        IConfigSchemaBuilder debugFileBuilder = new ConfigSchemaBuilder(configDir.resolve("jei-debug.ini"));
        DebugConfig.create(debugFileBuilder);
        debugFileBuilder.build().register();

        IConfigSchemaBuilder colorFileBuilder = new ConfigSchemaBuilder(configDir.resolve("jei-colors.ini"));
        ColorNameConfig colorNameConfig = new ColorNameConfig(colorFileBuilder);
        colorFileBuilder.build().register();

        IConfigSchemaBuilder modFileBuilder = new ConfigSchemaBuilder(configDir.resolve("jei-mod-id-format.ini"));
        ModIdFormatConfig modIdFormatConfig = new ModIdFormatConfig(modFileBuilder);
        modFileBuilder.build().register();

        EditModeConfigInternal editModeConfig = new EditModeConfigInternal(new EditModeConfigInternal.FileSerializer(configDir.resolve("blacklist.cfg")));
        RecipeCategorySortingConfig recipeCategorySortingConfig = new RecipeCategorySortingConfig(configDir.resolve("recipe-category-sort-order.ini"));

        return new ConfigData(
            editModeConfig,
            recipeCategorySortingConfig,
            modIdFormatConfig,
            colorNameConfig
        );
    }
}
