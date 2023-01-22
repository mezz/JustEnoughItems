package mezz.jei.gui.startup;

import mezz.jei.common.config.file.FileWatcher;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.ModNameSortingConfig;
import mezz.jei.common.platform.Services;
import mezz.jei.gui.config.BookmarkConfig;
import mezz.jei.gui.config.IBookmarkConfig;
import mezz.jei.gui.config.IJeiClientConfigs;
import mezz.jei.gui.config.JeiClientConfigs;

import java.nio.file.Path;

public record GuiConfigData(
    IJeiClientConfigs jeiClientConfigs,
    IBookmarkConfig bookmarkConfig,
    ModNameSortingConfig modNameSortingConfig,
    IngredientTypeSortingConfig ingredientTypeSortingConfig
) {
    public static GuiConfigData create(FileWatcher fileWatcher) {
        Path configDir = Services.PLATFORM.getConfigHelper().createJeiConfigDir();
        JeiClientConfigs jeiClientConfigs = new JeiClientConfigs(configDir.resolve("jei-client.ini"));
        jeiClientConfigs.register(fileWatcher);

        IBookmarkConfig bookmarkConfig = new BookmarkConfig(configDir);
        ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(configDir.resolve("ingredient-list-mod-sort-order.ini"));
        IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(configDir.resolve("ingredient-list-type-sort-order.ini"));

        return new GuiConfigData(
            jeiClientConfigs,
            bookmarkConfig,
            ingredientModNameSortingConfig,
            ingredientTypeSortingConfig
        );
    }
}
