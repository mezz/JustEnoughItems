package mezz.jei.common.startup;

import mezz.jei.common.config.IBookmarkConfig;
import mezz.jei.common.config.IEditModeConfig;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.common.config.sorting.RecipeCategorySortingConfig;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.core.config.IWorldConfig;

public record ConfigData(
    IClientConfig clientConfig,
    IEditModeConfig editModeConfig,
    IIngredientFilterConfig ingredientFilterConfig,
    IWorldConfig worldConfig,
    IBookmarkConfig bookmarkConfig,
    IIngredientGridConfig ingredientListConfig,
    IIngredientGridConfig bookmarkListConfig,
    RecipeCategorySortingConfig recipeCategorySortingConfig
) {
}
