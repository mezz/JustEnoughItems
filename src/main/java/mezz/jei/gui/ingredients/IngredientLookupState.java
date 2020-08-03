package mezz.jei.gui.ingredients;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.FocusedRecipes;
import mezz.jei.util.ErrorUtil;

import javax.annotation.Nullable;
import java.util.List;

public class IngredientLookupState {
	private final IRecipeManager recipeManager;
	@Nullable
	private final Focus<?> focus;
	private final ImmutableList<IRecipeCategory<?>> recipeCategories;

	private int recipeCategoryIndex;
	private int recipeIndex;
	private int recipesPerPage;
	private FocusedRecipes<?> focusedRecipes;

	public IngredientLookupState(IRecipeManager recipeManager, @Nullable Focus<?> focus, List<IRecipeCategory<?>> recipeCategories, int recipeCategoryIndex, int recipeIndex) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");
		Preconditions.checkArgument(recipeCategoryIndex >= 0, "Recipe category index cannot be negative.");
		Preconditions.checkArgument(recipeIndex >= 0, "Recipe index cannot be negative.");
		this.recipeManager = recipeManager;
		this.focus = focus;
		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
		this.recipeCategoryIndex = recipeCategoryIndex;
		this.recipeIndex = recipeIndex;
		this.focusedRecipes = updateFocusedRecipes();
	}

	@Nullable
	public Focus<?> getFocus() {
		return focus;
	}

	public ImmutableList<IRecipeCategory<?>> getRecipeCategories() {
		return recipeCategories;
	}

	public int getRecipeCategoryIndex() {
		return recipeCategoryIndex;
	}

	public void setRecipeCategoryIndex(int recipeCategoryIndex) {
		this.recipeCategoryIndex = recipeCategoryIndex;
		this.recipeIndex = 0;
		this.focusedRecipes = updateFocusedRecipes();
	}

	public int getRecipeIndex() {
		return recipeIndex;
	}

	public void setRecipeIndex(int recipeIndex) {
		this.recipeIndex = recipeIndex;
	}

	public int getRecipesPerPage() {
		return recipesPerPage;
	}

	public void setRecipesPerPage(int recipesPerPage) {
		this.recipesPerPage = recipesPerPage;
	}

	private FocusedRecipes<?> updateFocusedRecipes() {
		final IRecipeCategory<?> recipeCategory = recipeCategories.get(recipeCategoryIndex);
		return FocusedRecipes.create(focus, recipeManager, recipeCategory);
	}

	public FocusedRecipes<?> getFocusedRecipes() {
		return focusedRecipes;
	}
}
