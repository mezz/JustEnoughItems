package mezz.jei.common.config;

public interface IJeiClientConfigs {
    IClientConfig getClientConfig();

    IIngredientFilterConfig getIngredientFilterConfig();

    IIngredientGridConfig getIngredientListConfig();

    IIngredientGridConfig getBookmarkListConfig();
}
