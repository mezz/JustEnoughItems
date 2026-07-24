package mezz.jei.library.load.registration;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.registration.IAdvancedRegistration;
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

	public AdvancedRegistration(IJeiHelpers jeiHelpers) {
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public void addRecipeManagerPlugin(IRecipeManagerPlugin recipeManagerPlugin) {
		ErrorUtil.checkNotNull(recipeManagerPlugin, "recipeManagerPlugin");

		LOGGER.info("Added recipe manager plugin: {}", recipeManagerPlugin.getClass());
		recipeManagerPlugins.add(recipeManagerPlugin);
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

	@Unmodifiable
	public List<IRecipeManagerPlugin> getRecipeManagerPlugins() {
		return List.copyOf(recipeManagerPlugins);
	}

	@Unmodifiable
	public ImmutableListMultimap<RecipeType<?>, IRecipeCategoryDecorator<?>> getRecipeCategoryDecorators() {
		return recipeCategoryDecorators.toImmutable();
	}
}
