package mezz.jei.common.config;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;
import net.mezzdev.config.api.schema.builder.IConfigEditorCategoryBuilder;
import net.mezzdev.config.api.schema.builder.IConfigSchemaBuilder;
import net.mezzdev.config.api.sorting.ISortingConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientConfigs implements IClientConfigs {
	private final IClientConfig clientConfig;
	private final IIngredientFilterConfig ingredientFilterConfig;
	private final IIngredientGridConfig ingredientListConfig;
	private final IIngredientGridConfig bookmarkListConfig;
	private final ISortingConfig<String> recipeCategorySortingConfig;

	private final List<Runnable> runtimeListenerRemovals = new ArrayList<>();

	public ClientConfigs(
		IConfigSchemaBuilder builder,
		boolean isDev,
		ISortingConfig<String> recipeCategorySortingConfig
	) {
		IConfigCategoryBuilder search = builder.addCategory("search");
		IConfigCategoryBuilder ingredientList = builder.addCategory("ingredientList");
		IConfigEditorCategoryBuilder ingredientSorting = builder.addEditorCategory("ingredientSorting");
		IConfigCategoryBuilder bookmarkList = builder.addCategory("bookmarkList");
		IConfigCategoryBuilder input = builder.addCategory("input");
		IConfigCategoryBuilder recipes = builder.addCategory("recipes");
		IConfigCategoryBuilder tooltips = builder.addCategory("tooltips");
		IConfigCategoryBuilder lookups = builder.addCategory("lookups");
		IConfigCategoryBuilder cheating = builder.addCategory("cheating");
		IConfigCategoryBuilder advanced = builder.addCategory("advanced");

		IngredientFilterConfig ingredientFilterConfig = new IngredientFilterConfig(search);
		IngredientGridConfig ingredientListConfig = new IngredientGridConfig(ingredientList, HorizontalAlignment.RIGHT);
		IngredientGridConfig bookmarkListConfig = new IngredientGridConfig(bookmarkList, HorizontalAlignment.LEFT);

		this.clientConfig = new ClientConfig(
			search,
			ingredientList,
			ingredientSorting,
			bookmarkList,
			input,
			recipes,
			tooltips,
			lookups,
			cheating,
			advanced,
			isDev
		);
		this.ingredientFilterConfig = ingredientFilterConfig;
		this.ingredientListConfig = ingredientListConfig;
		this.bookmarkListConfig = bookmarkListConfig;
		this.recipeCategorySortingConfig = Objects.requireNonNull(recipeCategorySortingConfig);
		builder.build();
	}

	@Override
	public IClientConfig getClientConfig() {
		return clientConfig;
	}

	@Override
	public IIngredientFilterConfig getIngredientFilterConfig() {
		return ingredientFilterConfig;
	}

	@Override
	public IIngredientGridConfig getIngredientListConfig() {
		return ingredientListConfig;
	}

	@Override
	public IIngredientGridConfig getBookmarkListConfig() {
		return bookmarkListConfig;
	}

	@Override
	public ISortingConfig<String> getRecipeCategorySortingConfig() {
		return recipeCategorySortingConfig;
	}

	@Override
	public void registerRuntimeListenerRemoval(Runnable listenerRemoval) {
		synchronized (runtimeListenerRemovals) {
			runtimeListenerRemovals.add(Objects.requireNonNull(listenerRemoval));
		}
	}

	@Override
	public void onRuntimeStopped() {
		List<Runnable> listenerRemovals;
		synchronized (runtimeListenerRemovals) {
			listenerRemovals = List.copyOf(runtimeListenerRemovals);
			runtimeListenerRemovals.clear();
		}
		listenerRemovals.forEach(Runnable::run);
	}
}
