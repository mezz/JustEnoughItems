package mezz.jei.common.config;

import mezz.jei.core.config.IClientConfig;

public interface IJeiClientConfigs {
    IClientConfig getClientConfig();

    IIngredientFilterConfig getIngredientFilterConfig();

    IIngredientGridConfig getIngredientListConfig();

    IIngredientGridConfig getBookmarkListConfig();
}
