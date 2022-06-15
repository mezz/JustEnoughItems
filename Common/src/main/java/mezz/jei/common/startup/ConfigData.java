package mezz.jei.common.startup;

import mezz.jei.common.Internal;
import mezz.jei.common.config.BookmarkConfig;
import mezz.jei.common.config.EditModeConfig;
import mezz.jei.common.config.IBookmarkConfig;
import mezz.jei.common.config.IEditModeConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.IModIdFormatConfig;
import mezz.jei.common.config.JEIClientConfigs;
import mezz.jei.common.config.WorldConfig;
import mezz.jei.common.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.common.config.sorting.ModNameSortingConfig;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.common.input.IKeyBindings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        IConnectionToServer serverConnection,
        IKeyBindings keyBindings,
        Path configDir
    ) {
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create JEI config directory: " + configDir, e);
        }
        Internal.setServerConnection(serverConnection);

        Path configFile = configDir.resolve("jei-client.ini");
        JEIClientConfigs jeiClientConfigs = new JEIClientConfigs(configFile);
        jeiClientConfigs.register(configDir, configFile);

        IBookmarkConfig bookmarkConfig = new BookmarkConfig(configDir);
        IEditModeConfig editModeConfig = new EditModeConfig(configDir.resolve("blacklist.cfg"));
        RecipeCategorySortingConfig recipeCategorySortingConfig = new RecipeCategorySortingConfig(configDir.resolve("recipe-category-sort-order.ini"));
        ModNameSortingConfig ingredientModNameSortingConfig = new ModNameSortingConfig(configDir.resolve("ingredient-list-mod-sort-order.ini"));
        IngredientTypeSortingConfig ingredientTypeSortingConfig = new IngredientTypeSortingConfig(configDir.resolve("ingredient-list-type-sort-order.ini"));

        WorldConfig worldConfig = new WorldConfig(serverConnection, keyBindings);

        return new ConfigData(
            jeiClientConfigs.getClientConfig(),
            editModeConfig,
            jeiClientConfigs.getFilterConfig(),
            worldConfig,
            bookmarkConfig,
            jeiClientConfigs.getIngredientListConfig(),
            jeiClientConfigs.getBookmarkListConfig(),
            recipeCategorySortingConfig,
            jeiClientConfigs.getModIdFormat(),
            ingredientModNameSortingConfig,
            ingredientTypeSortingConfig
        );
    }
}
