package mezz.jei.gui.recipes;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class FocusedRecipes<T> {
	private final IRecipeManager recipeManager;
	private final IRecipeCategory<T> recipeCategory;
	private final @Nullable IFocus<?> focus;

	/**
	 * List of recipes for the currently selected recipeClass
	 */
	private @Nullable List<T> recipes;

	public static <T> FocusedRecipes<T> create(@Nullable IFocus<?> focus, IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		return new FocusedRecipes<>(focus, recipeManager, recipeCategory);
	}

	private FocusedRecipes(@Nullable IFocus<?> focus, IRecipeManager recipeManager, IRecipeCategory<T> recipeCategory) {
		this.focus = focus;
		this.recipeManager = recipeManager;
		this.recipeCategory = recipeCategory;
		this.recipes = null;
	}

	public IRecipeCategory<T> getRecipeCategory() {
		return recipeCategory;
	}

	public List<T> getRecipes() {
		if (recipes == null) {
			recipes = recipeManager.getRecipes(recipeCategory, focus, false);
		}
		return recipes;
	}
}
