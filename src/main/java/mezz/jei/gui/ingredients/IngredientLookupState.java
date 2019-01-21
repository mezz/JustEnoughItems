package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.Focus;
import mezz.jei.util.ErrorUtil;

public class IngredientLookupState {
	@Nullable
	private final Focus<?> focus;
	private final ImmutableList<IRecipeCategory> recipeCategories;

	private int recipeCategoryIndex;
	private int recipeIndex;
	private int recipesPerPage;

	public IngredientLookupState(@Nullable Focus<?> focus, List<IRecipeCategory> recipeCategories, int recipeCategoryIndex, int recipeIndex) {
		ErrorUtil.checkNotEmpty(recipeCategories, "recipeCategories");
		Preconditions.checkArgument(recipeCategoryIndex >= 0, "Recipe category index cannot be negative.");
		Preconditions.checkArgument(recipeIndex >= 0, "Recipe index cannot be negative.");
		this.focus = focus;
		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
		this.setRecipeCategoryIndex(recipeCategoryIndex);
		this.setRecipeIndex(recipeIndex);
	}

	@Nullable
	public Focus<?> getFocus() {
		return focus;
	}

	public ImmutableList<IRecipeCategory> getRecipeCategories() {
		return recipeCategories;
	}

	public int getRecipeCategoryIndex() {
		return recipeCategoryIndex;
	}

	public void setRecipeCategoryIndex(int recipeCategoryIndex) {
		this.recipeCategoryIndex = recipeCategoryIndex;
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
}
