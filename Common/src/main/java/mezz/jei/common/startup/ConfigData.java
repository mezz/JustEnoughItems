package mezz.jei.common.startup;

import mezz.jei.common.config.BookmarkConfig;
import mezz.jei.common.config.EditModeConfig;
import mezz.jei.common.config.IBookmarkConfig;
import mezz.jei.common.config.IEditModeConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IModIdFormatConfig;
import mezz.jei.common.config.WorldConfig;
import mezz.jei.common.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.common.config.sorting.ModNameSortingConfig;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;

import java.io.File;

public record ConfigData(
    IClientConfig clientConfig,
    IEditModeConfig editModeConfig,
    IIngredientFilterConfig ingredientFilterConfig,
    IWorldConfig worldConfig,
    IBookmarkConfig bookmarkConfig,
    IIngredientGridConfig ingredientListConfig,
    IIngredientGridConfig bookmarkListConfig,
    RecipeCategorySortingConfig recipeCategorySortingConfig,
    IModIdFormatConfig modIdFormatConfig,
    ModNameSortingConfig modNameSortingConfig,
    IngredientTypeSortingConfig ingredientTypeSortingConfig
) {
    public static ConfigData create(
        IClientConfig clientConfig,
        IIngredientFilterConfig filterConfig,
        IIngredientGridConfig ingredientListConfig,
        IIngredientGridConfig bookmarkListConfig,
        IModIdFormatConfig modIdFormatConfig,
        IConnectionToServer serverConnection,
        IKeyBindings keyBindings,
        File jeiConfigurationDir
    ) {
        // Additional config files
        IBookmarkConfig bookmarkConfig = new BookmarkConfig(jeiConfigurationDir);
        IEditModeConfig editModeConfig = new EditModeConfig(jeiConfigurationDir);
        RecipeCategorySortingConfig recipeCategorySortingConfig = new RecipeCategorySortingConfig(new File(jeiConfigurationDir, "recipe-category-sort-order.ini"));
        WorldConfig worldConfig = new WorldConfig(serverConnection, keyBindings);

        ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(new File(jeiConfigurationDir, "ingredient-list-mod-sort-order.ini"));
        IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(new File(jeiConfigurationDir, "ingredient-list-type-sort-order.ini"));

        return new ConfigData(
            clientConfig,
            editModeConfig,
            filterConfig,
            worldConfig,
            bookmarkConfig,
            ingredientListConfig,
            bookmarkListConfig,
            recipeCategorySortingConfig,
            modIdFormatConfig,
            ingredientModNameSortingConfig,
            ingredientTypeSortingConfig
        );
    }
}
