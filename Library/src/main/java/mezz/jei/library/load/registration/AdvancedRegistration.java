package mezz.jei.library.load.registration;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.advanced.IRecipeManagerPluginHelper;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.runtime.IJeiFeatures;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.collect.ListMultiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class AdvancedRegistration implements IAdvancedRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<IRecipeManagerPlugin> recipeManagerPlugins = new ArrayList<>();
	private final ListMultiMap<RecipeType<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators = new ListMultiMap<>();
	private final IJeiHelpers jeiHelpers;
	private final IJeiFeatures jeiFeatures;
	private final IRecipeManagerPluginHelper pluginHelper;

	public AdvancedRegistration(IJeiHelpers jeiHelpers, IJeiFeatures jeiFeatures, IRecipeManagerPluginHelper pluginHelper) {
		this.jeiHelpers = jeiHelpers;
		this.jeiFeatures = jeiFeatures;
		this.pluginHelper = pluginHelper;
	}

	@Override
	public void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin) {
		ErrorUtil.checkNotNull(recipeManagerPlugin, "recipeManagerPlugin");

		LOGGER.info("Added recipe manager plugin: {}", recipeManagerPlugin.getClass());
		recipeManagerPlugins.add(recipeManagerPlugin);
	}

	@Override
	public <T> void addTypedRecipeManagerPlugin(RecipeType<T> recipeType, ISimpleRecipeManagerPlugin<T> recipeManagerPlugin) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(recipeManagerPlugin, "recipeManagerPlugin");

		TypedRecipeManagerPluginAdapter<T> adapter = new TypedRecipeManagerPluginAdapter<>(pluginHelper, recipeType, recipeManagerPlugin);
		LOGGER.info("Added typed recipe manager plugin: {}", recipeManagerPlugin.getClass());
		recipeManagerPlugins.add(adapter);
	}

	@Override
	public <T> void addRecipeCategoryDecorator(RecipeType<T> recipeType, IRecipeCategoryDecorator<T> decorator) {
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		ErrorUtil.checkNotNull(decorator, "decorator");

		LOGGER.info("Added recipe category decorator: {} for recipe type: {}", decorator.getClass(), recipeType.getUid());
		recipeCategoryDecorators.put(recipeType, decorator);
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IJeiFeatures getJeiFeatures() {
		return jeiFeatures;
	}

	@Override
	public IRecipeManagerPluginHelper getRecipeManagerPluginHelper() {
		return pluginHelper;
	}

	@Unmodifiable
	public List<IRecipeManagerPlugin> getRecipeManagerPlugins() {
		return List.copyOf(recipeManagerPlugins);
	}

	@Unmodifiable
	public ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> getRecipeCategoryDecorators() {
		return recipeCategoryDecorators.toImmutable();
	}
}
