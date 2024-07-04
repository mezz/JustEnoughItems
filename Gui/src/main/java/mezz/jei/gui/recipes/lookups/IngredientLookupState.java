package mezz.jei.gui.recipes.lookups;

import com.google.common.base.Preconditions;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.util.MathUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class IngredientLookupState implements ILookupState {
	private final IRecipeManager recipeManager;
	private final IFocusGroup focuses;
	@Unmodifiable
	private final List<IRecipeCategory<?>> recipeCategories;

	private int recipeCategoryIndex;
	private int recipeIndex;
	private int recipesPerPage;
	@Nullable
	private IFocusedRecipes<?> focusedRecipes;

	public static ILookupState createWithFocus(IRecipeManager recipeManager, IFocusGroup focuses) {
		List<IRecipeCategory<?>> recipeCategories = recipeManager.createRecipeCategoryLookup()
			.limitFocus(focuses.getAllFocuses())
			.get()
			.toList();

		return new IngredientLookupState(recipeManager, focuses, recipeCategories);
	}

	public static ILookupState createWithCategories(IRecipeManager recipeManager, IFocusFactory focusFactory, List<IRecipeCategory<?>> recipeCategories) {
		return new IngredientLookupState(recipeManager, focusFactory.getEmptyFocusGroup(), recipeCategories);
	}

	private IngredientLookupState(IRecipeManager recipeManager, IFocusGroup focuses, List<IRecipeCategory<?>> recipeCategories) {
		this.recipeManager = recipeManager;
		this.focuses = focuses;
		this.recipeCategories = List.copyOf(recipeCategories);
	}

	@Override
	public IFocusGroup getFocuses() {
		return focuses;
	}

	@Override
	@Unmodifiable
	public List<IRecipeCategory<?>> getRecipeCategories() {
		return recipeCategories;
	}

	public int getRecipeCategoryIndex() {
		return recipeCategoryIndex;
	}

	@Override
	public boolean moveToRecipeCategory(IRecipeCategory<?> recipeCategory) {
		final int recipeCategoryIndex = recipeCategories.indexOf(recipeCategory);
		if (recipeCategoryIndex >= 0) {
			this.moveToRecipeCategoryIndex(recipeCategoryIndex);
			return true;
		}
		return false;
	}

	@Override
	public void moveToRecipeCategoryIndex(int recipeCategoryIndex) {
		Preconditions.checkArgument(recipeCategoryIndex >= 0, "Recipe category index cannot be negative.");
		this.recipeCategoryIndex = recipeCategoryIndex;
		this.recipeIndex = 0;
		this.focusedRecipes = null;
	}

	@Override
	public void nextRecipeCategory() {
		final int recipesTypesCount = getRecipeCategories().size();
		moveToRecipeCategoryIndex((getRecipeCategoryIndex() + 1) % recipesTypesCount);
	}

	@Override
	public void previousRecipeCategory() {
		final int recipesTypesCount = getRecipeCategories().size();
		moveToRecipeCategoryIndex((recipesTypesCount + getRecipeCategoryIndex() - 1) % recipesTypesCount);
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

	@Override
	public int getRecipeIndex() {
		return recipeIndex;
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
	public IFocusedRecipes<?> getFocusedRecipes() {
		if (focusedRecipes == null) {
			final IRecipeCategory<?> recipeCategory = recipeCategories.get(recipeCategoryIndex);
			focusedRecipes = FocusedRecipes.create(focuses, recipeManager, recipeCategory);
		}
		return focusedRecipes;
	}
}
