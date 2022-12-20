package mezz.jei.gui.ingredients;

import com.google.common.base.Preconditions;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.recipes.FocusedRecipes;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class IngredientLookupState {
	private final IRecipeManager recipeManager;
	private final IFocusGroup focuses;
	@Unmodifiable
	private final List<IRecipeCategory<?>> recipeCategories;

	private int recipeCategoryIndex;
	private int recipeIndex;
	private int recipesPerPage;
	@Nullable
	private FocusedRecipes<?> focusedRecipes;

	public static IngredientLookupState createWithFocus(IRecipeManager recipeManager, IFocusGroup focuses) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.limitFocus(focuses.getAllFocuses())
			.get()
			.toList();

		return new IngredientLookupState(recipeManager, focuses, recipeCategories);
	}

	public static IngredientLookupState createWithCategories(IRecipeManager recipeManager, IFocusFactory focusFactory, List<IRecipeCategory<?>> recipeCategories) {
		return new IngredientLookupState(recipeManager, focusFactory.getEmptyFocusGroup(), recipeCategories);
	}

	private IngredientLookupState(IRecipeManager recipeManager, IFocusGroup focuses, List<IRecipeCategory<?>> recipeCategories) {
		this.recipeManager = recipeManager;
		this.focuses = focuses;
		this.recipeCategories = List.copyOf(recipeCategories);
	}

	public IFocusGroup getFocuses() {
		return focuses;
	}

	@Unmodifiable
	public List<IRecipeCategory<?>> getRecipeCategories() {
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
			focusedRecipes = FocusedRecipes.create(focuses, recipeManager, recipeCategory);
		}
		return focusedRecipes;
	}
}
