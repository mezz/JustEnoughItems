package mezz.jei.gui.startup;

import mezz.jei.common.config.BookmarkConfig;
import mezz.jei.common.config.EditModeConfigInternal;
import mezz.jei.common.config.IBookmarkConfig;
import mezz.jei.gui.config.IJeiClientConfigs;
import mezz.jei.gui.config.JeiClientConfigs;
import mezz.jei.common.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.common.config.sorting.ModNameSortingConfig;
import mezz.jei.common.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record GuiConfigData(
    IJeiClientConfigs jeiClientConfigs,
    EditModeConfigInternal editModeConfigInternal,
    IBookmarkConfig bookmarkConfig,
    ModNameSortingConfig modNameSortingConfig,
    IngredientTypeSortingConfig ingredientTypeSortingConfig
) {
    public static GuiConfigData create() {
        Path configDir = Services.PLATFORM.getConfigDir();
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create JEI config directory: " + configDir, e);
        }

        JeiClientConfigs jeiClientConfigs = new JeiClientConfigs(configDir.resolve("jei-client.ini"));
        jeiClientConfigs.register();

        IBookmarkConfig bookmarkConfig = new BookmarkConfig(configDir);
        EditModeConfigInternal editModeConfigInternal = new EditModeConfigInternal(new EditModeConfigInternal.FileSerializer(configDir.resolve("blacklist.cfg")));
        ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(configDir.resolve("ingredient-list-mod-sort-order.ini"));
        IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(configDir.resolve("ingredient-list-type-sort-order.ini"));

        return new GuiConfigData(
            jeiClientConfigs,
            editModeConfigInternal,
            bookmarkConfig,
            ingredientModNameSortingConfig,
            ingredientTypeSortingConfig
        );
    }
}
