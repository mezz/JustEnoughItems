package mezz.jei.library.recipes;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeLookup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.focus.FocusGroup;

import java.util.Collection;
import java.util.stream.Stream;

public class RecipeLookup<R> implements IRecipeLookup<R> {
	private final RecipeType<R> recipeType;
	private final RecipeManagerInternal recipeManager;
	private final IIngredientManager ingredientManager;

	private boolean includeHidden = false;
	private IFocusGroup focusGroup = FocusGroup.EMPTY;

	public RecipeLookup(RecipeType<R> recipeType, RecipeManagerInternal recipeManager, IIngredientManager ingredientManager) {
		this.recipeType = recipeType;
		this.recipeManager = recipeManager;
		this.ingredientManager = ingredientManager;
	}

	@Override
	public IRecipeLookup<R> limitFocus(Collection<? extends IFocus<?>> focuses) {
		this.focusGroup = FocusGroup.create(focuses, ingredientManager);
		return this;
	}

	@Override
	public IRecipeLookup<R> includeHidden() {
		this.includeHidden = true;
		return this;
	}

	@Override
	public Stream<R> get() {
		return recipeManager.getRecipesStream(recipeType, focusGroup, includeHidden);
	}
}
