package mezz.jei.gui.config;

import mezz.jei.common.config.IIngredientFilterConfig;

public interface IJeiClientConfigs {
    IClientConfig getClientConfig();

    IIngredientFilterConfig getIngredientFilterConfig();

    IIngredientGridConfig getIngredientListConfig();

    IIngredientGridConfig getBookmarkListConfig();
}
