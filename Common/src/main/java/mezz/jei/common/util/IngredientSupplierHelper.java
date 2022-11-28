package mezz.jei.common.util;

import mezz.jei.api.ingredients.IRegisteredIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.focus.FocusGroup;
import mezz.jei.common.gui.recipes.layout.RecipeLayoutBuilder;
import mezz.jei.common.ingredients.IIngredientSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public final class IngredientSupplierHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	private IngredientSupplierHelper() {
	}

	@Nullable
	public static <T> IIngredientSupplier getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory, IRegisteredIngredients registeredIngredients) {
		try {
			RecipeLayoutBuilder builder = new RecipeLayoutBuilder(registeredIngredients, 0);
			recipeCategory.setRecipe(builder, recipe, FocusGroup.EMPTY);
			if (builder.isUsed()) {
				return builder;
			}
		} catch (RuntimeException | LinkageError e) {
			String recipeName = RecipeErrorUtil.getNameForRecipe(recipe);
			LOGGER.error("Found a broken recipe, failed to setRecipe with RecipeLayoutBuilder: {}\n", recipeName, e);
		}

		return null;
	}
}
