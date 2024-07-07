package mezz.jei.gui.recipes.lookups;

import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.MathUtil;

import java.util.List;

public class SingleCategoryLookupState implements ILookupState {
	private final IFocusedRecipes<?> focusedRecipes;
	private final IFocusGroup focusGroup;
	private int recipesPerPage = 1;
	private int recipeIndex;

	public SingleCategoryLookupState(IFocusedRecipes<?> focusedRecipes, IFocusGroup focusGroup) {
		this.focusedRecipes = focusedRecipes;
		this.focusGroup = focusGroup;
	}

	@Override
	public List<IRecipeCategory<?>> getRecipeCategories() {
		return List.of(focusedRecipes.getRecipeCategory());
	}

	@Override
	public boolean moveToRecipeCategory(IRecipeCategory<?> recipeCategory) {
		RecipeType<?> recipeType = focusedRecipes.getRecipeCategory().getRecipeType();
		return recipeCategory.getRecipeType().equals(recipeType);
	}

	@Override
	public int getRecipesPerPage() {
		return recipesPerPage;
	}

	@Override
	public void setRecipesPerPage(int recipesPerPage) {
		this.recipesPerPage = recipesPerPage;
	}

	@Override
	public int getRecipeIndex() {
		return recipeIndex;
	}

	@Override
	public IFocusGroup getFocuses() {
		return focusGroup;
	}

	@Override
	public IFocusedRecipes<?> getFocusedRecipes() {
		return focusedRecipes;
	}

	@Override
	public void nextRecipeCategory() {

	}

	@Override
	public void previousRecipeCategory() {

	}

	@Override
	public void goToFirstPage() {
		this.recipeIndex = 0;
	}

	@Override
	public void nextPage() {
		int recipeCount = recipeCount();
		this.recipeIndex = recipeIndex + recipesPerPage;
		if (recipeIndex >= recipeCount) {
			this.recipeIndex = 0;
		}
	}

	@Override
	public void previousPage() {
		this.recipeIndex = recipeIndex - recipesPerPage;
		if (recipeIndex < 0) {
			final int pageCount = pageCount();
			this.recipeIndex = (pageCount - 1) * recipesPerPage;
		}
	}

	public int recipeCount() {
		return getFocusedRecipes().getRecipes().size();
	}

	@Override
	public int pageCount() {
		int recipeCount = recipeCount();
		if (recipeCount <= 1) {
			return 1;
		}

		return MathUtil.divideCeil(recipeCount, recipesPerPage);
	}
}
