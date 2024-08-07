package mezz.jei.library.util;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import mezz.jei.library.ingredients.IIngredientSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class IngredientSupplierHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	private IngredientSupplierHelper() {
	}

	@Nullable
	public static <T> IIngredientSupplier getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory, IIngredientManager ingredientManager) {
		if (!recipeCategory.isHandled(recipe)) {
			return null;
		}
		try {
			IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager);
			recipeCategory.setRecipe(builder, recipe, FocusGroup.EMPTY);
			return builder.buildIngredientSupplier();
		} catch (RuntimeException | LinkageError e) {
			String recipeName = RecipeErrorUtil.getNameForRecipe(recipe);
			LOGGER.error("Found a broken recipe, failed to setRecipe with RecipeLayoutBuilder: {}\n", recipeName, e);
		}

		return null;
	}
}
