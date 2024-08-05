package mezz.jei.library.util;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.recipes.RecipeLayoutBuilder;
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
		try {
			RecipeLayoutBuilder<T> builder = new RecipeLayoutBuilder<>(recipeCategory, recipe, ingredientManager);
			recipeCategory.setRecipe(builder, recipe, FocusGroup.EMPTY);
			// as a minor optimization, skip setting widgets that have no slots (IRecipeCategory#createWidgets)
			if (!builder.isEmpty()) {
				return builder.buildIngredientSupplier();
			} else {
				String recipeName = RecipeErrorUtil.getNameForRecipe(recipe);
				LOGGER.warn("The recipe category for '{}' failed to set anything in its setRecipe method, for recipe: {}", recipeCategory.getRecipeType(), recipeName);
			}
		} catch (RuntimeException | LinkageError e) {
			String recipeName = RecipeErrorUtil.getNameForRecipe(recipe);
			LOGGER.error("Found a broken recipe, failed to setRecipe with RecipeLayoutBuilder: {}\n", recipeName, e);
		}

		return null;
	}
}
