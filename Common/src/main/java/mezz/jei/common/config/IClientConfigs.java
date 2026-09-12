package mezz.jei.common.config;

public interface IClientConfigs {
	IClientConfig getClientConfig();

	IIngredientFilterConfig getIngredientFilterConfig();

	IIngredientGridConfig getIngredientListConfig();

	IIngredientGridConfig getBookmarkListConfig();

	void onRuntimeStopped();
}
