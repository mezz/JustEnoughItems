package mezz.jei.gui.recipes.lookups;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

	default <T> List<T> getVisible(List<? extends T> recipes) {
		final int firstRecipeIndex = getRecipeIndex() - (getRecipeIndex() % getRecipesPerPage());
		final int maxIndex = Math.min(recipes.size(), firstRecipeIndex + getRecipesPerPage());
		return IntStream.range(firstRecipeIndex, maxIndex)
			.mapToObj(recipes::get)
			.collect(Collectors.toList());
	}
}
