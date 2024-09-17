package mezz.jei.gui.recipes.lookups;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayoutWithButtons;
import mezz.jei.gui.recipes.layouts.IRecipeLayoutList;

import java.util.List;

public interface ILookupState {
	List<IRecipeCategory<?>> getRecipeCategories();

	boolean moveToRecipeCategory(IRecipeCategory<?> recipeCategory);

	int getRecipesPerPage();

	void setRecipesPerPage(int recipesPerPage);

	int getRecipeIndex();

	IFocusGroup getFocuses();

	IFocusedRecipes<?> getFocusedRecipes();

	void nextRecipeCategory();

	void previousRecipeCategory();

	void goToFirstPage();

	void nextPage();

	void previousPage();

	int pageCount();

	default List<RecipeLayoutWithButtons<?>> getVisible(IRecipeLayoutList recipes) {
		final int recipesPerPage = getRecipesPerPage();
		final int firstRecipeIndex = getRecipeIndex() - (getRecipeIndex() % recipesPerPage);
		final int maxIndex = Math.min(recipes.size(), firstRecipeIndex + recipesPerPage);
		if (firstRecipeIndex >= maxIndex) {
			return List.of();
		}
		return recipes.subList(firstRecipeIndex, maxIndex);
	}
}
