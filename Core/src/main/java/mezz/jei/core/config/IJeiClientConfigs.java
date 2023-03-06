package mezz.jei.core.config;

public interface IJeiClientConfigs {
    IClientConfig getClientConfig();

    IIngredientFilterConfig getIngredientFilterConfig();

    IIngredientGridConfig getIngredientListConfig();

    IIngredientGridConfig getBookmarkListConfig();
}
