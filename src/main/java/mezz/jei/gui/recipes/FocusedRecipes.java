package mezz.jei.gui.recipes;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;

import javax.annotation.Nullable;
import java.util.List;

public class FocusedRecipes<T> {
	private final IRecipeCategory<T> recipeCategory;
	/**
	 * List of recipes for the currently selected recipeClass
	 */
	private final List<T> recipes;

	public static <T> FocusedRecipes<T> create(@Nullable IFocus<?> focus, IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		final List<T> recipes;
		if (focus != null) {
			recipes = recipeManager.getRecipes(recipeCategory, focus);
		} else {
			recipes = recipeManager.getRecipes(recipeCategory);
		}
		return new FocusedRecipes<>(recipeCategory, recipes);
	}

	private FocusedRecipes(IRecipeCategory<T> recipeCategory, List<T> recipes) {
		this.recipeCategory = recipeCategory;
		this.recipes = recipes;
	}

	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	public List<T> getRecipes() {
		return recipes;
	}
}
