package mezz.jei.gui.config;

import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.config.IIngredientGridConfig;
import mezz.jei.core.config.IClientConfig;

public interface IJeiClientConfigs {
    IClientConfig getClientConfig();

    IIngredientFilterConfig getIngredientFilterConfig();

    IIngredientGridConfig getIngredientListConfig();

    IIngredientGridConfig getBookmarkListConfig();
}
