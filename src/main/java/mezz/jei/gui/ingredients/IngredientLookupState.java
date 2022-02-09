package mezz.jei.gui.ingredients;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.FocusedRecipes;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class IngredientLookupState {
	private final IRecipeManager recipeManager;
	@Nullable
	private final Focus<?> focus;
	private final ImmutableList<IRecipeCategory<?>> recipeCategories;

	private int recipeCategoryIndex;
	private int recipeIndex;
	private int recipesPerPage;
	@Nullable
	private FocusedRecipes<?> focusedRecipes;

	public static IngredientLookupState createWithFocus(IRecipeManager recipeManager, @Nullable Focus<?> focus) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.getRecipeCategories(focus, false);
		return new IngredientLookupState(recipeManager, focus, recipeCategories);
	}

	public static IngredientLookupState createWithCategories(IRecipeManager recipeManager, List<IRecipeCategory<?>> recipeCategories) {
		return new IngredientLookupState(recipeManager, null, recipeCategories);
	}

	private IngredientLookupState(IRecipeManager recipeManager, @Nullable Focus<?> focus, List<IRecipeCategory<?>> recipeCategories) {
		this.recipeManager = recipeManager;
		this.focus = focus;
		this.recipeCategories = ImmutableList.copyOf(recipeCategories);
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

	public boolean setRecipeCategory(IRecipeCategory<?> recipeCategory) {
		final int recipeCategoryIndex = recipeCategories.indexOf(recipeCategory);
		if (recipeCategoryIndex >= 0) {
			this.setRecipeCategoryIndex(recipeCategoryIndex);
			return true;
		}
		return false;
	}

	public void setRecipeCategoryIndex(int recipeCategoryIndex) {
		Preconditions.checkArgument(recipeCategoryIndex >= 0, "Recipe category index cannot be negative.");
		this.recipeCategoryIndex = recipeCategoryIndex;
		this.recipeIndex = 0;
		this.focusedRecipes = null;
	}

	public void nextRecipeCategory() {
		final int recipesTypesCount = getRecipeCategories().size();
		setRecipeCategoryIndex((getRecipeCategoryIndex() + 1) % recipesTypesCount);
	}

	public void previousRecipeCategory() {
		final int recipesTypesCount = getRecipeCategories().size();
		setRecipeCategoryIndex((recipesTypesCount + getRecipeCategoryIndex() - 1) % recipesTypesCount);
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

	public FocusedRecipes<?> getFocusedRecipes() {
		if (focusedRecipes == null) {
			final IRecipeCategory<?> recipeCategory = recipeCategories.get(recipeCategoryIndex);
			focusedRecipes = FocusedRecipes.create(focus, recipeManager, recipeCategory);
		}
		return focusedRecipes;
	}
}
