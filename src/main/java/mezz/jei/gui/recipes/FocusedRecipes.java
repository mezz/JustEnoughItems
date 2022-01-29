package mezz.jei.gui.recipes;

import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.Focus;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class FocusedRecipes<T> {
	private final IRecipeManager recipeManager;
	private final IRecipeCategory<T> recipeCategory;
	private final List<Focus<?>> focuses;

	/**
	 * List of recipes for the currently selected recipeClass
	 */
	private @Nullable List<T> recipes;

	public static <T> FocusedRecipes<T> create(List<Focus<?>> focuses, IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		return new FocusedRecipes<>(focuses, recipeManager, recipeCategory);
	}

	private FocusedRecipes(List<Focus<?>> focuses, IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		this.focuses = focuses;
		this.recipeManager = recipeManager;
		this.recipeCategory = recipeCategory;
		this.recipes = null;
	}

	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	public List<T> getRecipes() {
		if (recipes == null) {
			recipes = recipeManager.getRecipes(recipeCategory, focuses, false);
		}
		return recipes;
	}
}
