package mezz.jei.api.config;

public interface IClientConfigs {
    IClientConfig getClientConfig();

    IIngredientFilterConfig getIngredientFilterConfig();

    IIngredientGridConfig getIngredientListConfig();

    IIngredientGridConfig getBookmarkListConfig();
}
