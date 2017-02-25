package mezz.jei.gui.recipes;

import javax.annotation.Nullable;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;

public class IngredientLookupState {
	@Nullable
	private final IFocus<?> focus;
	private final ImmutableList<IRecipeCategory> recipeCategories;

	private int recipeCategoryIndex;
	private int recipeIndex;
	private int recipesPerPage;

	public IngredientLookupState(@Nullable IFocus<?> focus, List<IRecipeCategory> recipeCategories, int recipeCategoryIndex, int recipeIndex) {
		Preconditions.checkArgument(!recipeCategories.isEmpty(), "Recipe categories cannot be empty.");
		Preconditions.checkArgument(recipeCategoryIndex >= 0, "Recipe category index cannot be negative.");
		Preconditions.checkArgument(recipeIndex >= 0, "Recipe index cannot be negative.");
		this.focus = focus;
		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
		this.setRecipeCategoryIndex(recipeCategoryIndex);
		this.setRecipeIndex(recipeIndex);
	}

	@Nullable
	public IFocus<?> getFocus() {
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
		Internal.getIngredientLookupMemory().markDirty();
	}

	public int getRecipeIndex() {
		return recipeIndex;
	}

	public void setRecipeIndex(int recipeIndex) {
		this.recipeIndex = recipeIndex;
		Internal.getIngredientLookupMemory().markDirty();
	}

	public int getRecipesPerPage() {
		return recipesPerPage;
	}

	public void setRecipesPerPage(int recipesPerPage) {
		this.recipesPerPage = recipesPerPage;
	}
}
