package mezz.jei.gui.recipes.lookups;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.List;

public interface ILookupState {
	List<IRecipeCategory<?>> getRecipeCategories();

	void moveToRecipeCategoryIndex(int recipeCategoryIndex);

	boolean moveToRecipeCategory(IRecipeCategory<?> recipeCategory);

	int getRecipesPerPage();

	void setRecipesPerPage(int recipesPerPage);

	int getRecipeIndex();

	IFocusGroup getFocuses();

	IFocusedRecipes<?> getFocusedRecipes();

	void nextRecipeCategory();

	void previousRecipeCategory();

	void nextPage();

	void previousPage();

	int pageCount();
}
