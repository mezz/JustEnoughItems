package mezz.jei.common.recipes;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeCategoriesLookup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.focus.FocusGroup;
import mezz.jei.common.util.ErrorUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class RecipeCategoriesLookup implements IRecipeCategoriesLookup {
	private final RecipeManagerInternal recipeManager;
	private final RegisteredIngredients registeredIngredients;

	private boolean includeHidden = false;
	private Collection<RecipeType<?>> recipeTypes = List.of();
	private IFocusGroup focusGroup = FocusGroup.EMPTY;

	public RecipeCategoriesLookup(RecipeManagerInternal recipeManager, RegisteredIngredients registeredIngredients) {
		this.recipeManager = recipeManager;
		this.registeredIngredients = registeredIngredients;
	}

	@Override
	public IRecipeCategoriesLookup limitTypes(Collection<RecipeType<?>> recipeTypes) {
		ErrorUtil.checkNotNull(recipeTypes, "recipeTypes");
		this.recipeTypes = recipeTypes;
		return this;
	}

	@Override
	public IRecipeCategoriesLookup limitFocus(Collection<? extends IFocus<?>> focuses) {
		ErrorUtil.checkNotNull(focuses, "focuses");
		this.focusGroup = FocusGroup.create(focuses, registeredIngredients);
		return this;
	}

	@Override
	public IRecipeCategoriesLookup includeHidden() {
		this.includeHidden = true;
		return this;
	}

	@Override
	public Stream<IRecipeCategory<?>> get() {
		return recipeManager.getRecipeCategoriesForTypes(recipeTypes, focusGroup, includeHidden);
	}
}
