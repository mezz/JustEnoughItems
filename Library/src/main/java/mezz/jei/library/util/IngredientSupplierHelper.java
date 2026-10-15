package mezz.jei.library.util;

import mezz.jei.library.ingredients.RecipeIngredientSupplier;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.recipes.supplier.builder.IngredientSupplierBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.Set;

public final class IngredientSupplierHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	private IngredientSupplierHelper() {
	}

	public static <T> RecipeIngredientSupplier getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory, IIngredientManager ingredientManager) {
		return getIngredientSupplier(recipe, recipeCategory, ingredientManager, EnumSet.allOf(RecipeIngredientRole.class));
	}

	public static <T> RecipeIngredientSupplier getIngredientSupplier(T recipe, IRecipeCategory<T> recipeCategory, IIngredientManager ingredientManager, Set<RecipeIngredientRole> roles) {
		IngredientSupplierBuilder builder = new IngredientSupplierBuilder(ingredientManager, roles);
		if (!recipeCategory.isHandled(recipe)) {
			return builder.buildIngredientSupplier();
		}
		try {
			recipeCategory.setRecipe(builder, recipe, FocusGroup.EMPTY);
		} catch (RuntimeException | LinkageError e) {
			String recipeInfo = ErrorUtil.getRecipeInfo(recipeCategory, recipe);
			LOGGER.error("Found a broken recipe, failed to setRecipe with RecipeLayoutBuilder:\n{}", recipeInfo, e);
		}

		return builder.buildIngredientSupplier();
	}
}
