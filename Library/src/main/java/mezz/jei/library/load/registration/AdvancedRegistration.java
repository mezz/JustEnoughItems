package mezz.jei.library.load.registration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.common.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class AdvancedRegistration implements IAdvancedRegistration {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<IRecipeManagerPlugin> recipeManagerPlugins = new ArrayList<>();
	private final Multimap<IRecipeCategory<?>, IRecipeCategoryDecorator<?>> recipeCategoryDecorators = HashMultimap.create();
	private final List<IRecipeCategory<?>> recipeCategories;
	private final IJeiHelpers jeiHelpers;

	public AdvancedRegistration(List<IRecipeCategory<?>> recipeCategories, IJeiHelpers jeiHelpers) {
		this.recipeCategories = recipeCategories;
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

		for (IRecipeCategory<?> recipeCategory : recipeCategories) {
			var type = recipeCategory.getRecipeType();
			if (type.equals(recipeType)) {
				LOGGER.info("Added global recipe category decorator: {} for recipe category: {}", decorator.getClass(), recipeCategory.getRecipeType().getUid());
				recipeCategoryDecorators.put(recipeCategory, decorator);
			}
		}
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
	public Multimap<IRecipeCategory<?>, IRecipeCategoryDecorator<?>> getRecipeCategoryDecorators() {
		return ImmutableMultimap.copyOf(recipeCategoryDecorators);
	}
}
