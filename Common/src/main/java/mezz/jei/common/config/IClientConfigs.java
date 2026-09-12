package mezz.jei.common.config;

import net.mezzdev.config.api.sorting.ISortingConfig;

public interface IClientConfigs {
	IClientConfig getClientConfig();

	IIngredientFilterConfig getIngredientFilterConfig();

	IIngredientGridConfig getIngredientListConfig();

	IIngredientGridConfig getBookmarkListConfig();

	ISortingConfig<String> getRecipeCategorySortingConfig();

	void registerRuntimeListenerRemoval(Runnable listenerRemoval);

	void onRuntimeStopped();
}
