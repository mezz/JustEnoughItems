package mezz.jei.library.util;

import mezz.jei.api.ingredients.IIngredientSupplier;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IngredientSupplierHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	private IngredientSupplierHelper() {
	}

	public static <T> IIngredientSupplier getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory, IIngredientManager ingredientManager) {
		IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager);
		if (!recipeCategory.isHandled(recipe)) {
			return builder.buildIngredientSupplier();
		}
		try {
			recipeCategory.setRecipe(builder, recipe, FocusGroup.EMPTY);
		} catch (RuntimeException | LinkageError e) {
			String recipeName = RecipeErrorUtil.getNameForRecipe(recipe);
			LOGGER.error("Found a broken recipe, failed to setRecipe with RecipeLayoutBuilder: {}\n", recipeName, e);
		}

		return builder.buildIngredientSupplier();
	}
}
