package mezz.jei.common.runtime;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.common.ingredients.RegisteredIngredients;
import mezz.jei.common.ingredients.TypedIngredient;
import mezz.jei.common.util.ErrorUtil;

import java.util.Optional;

public class JeiRuntime implements IJeiRuntime {
	private final IRecipeManager recipeManager;
	private final IIngredientListOverlay ingredientListOverlay;
	private final IBookmarkOverlay bookmarkOverlay;
	private final IRecipesGui recipesGui;
	private final IIngredientFilter ingredientFilter;
	private final RegisteredIngredients registeredIngredients;
	private final IIngredientManager ingredientManager;
	private final IIngredientVisibility ingredientVisibility;
	private final IJeiHelpers jeiHelpers;

	public JeiRuntime(
		IRecipeManager recipeManager,
		IIngredientListOverlay ingredientListOverlay,
		IBookmarkOverlay bookmarkOverlay,
		IRecipesGui recipesGui,
		IIngredientFilter ingredientFilter,
		RegisteredIngredients registeredIngredients,
		IIngredientManager ingredientManager,
		IIngredientVisibility ingredientVisibility,
		IJeiHelpers jeiHelpers
	) {
		this.recipeManager = recipeManager;
		this.ingredientListOverlay = ingredientListOverlay;
		this.bookmarkOverlay = bookmarkOverlay;
		this.recipesGui = recipesGui;
		this.ingredientFilter = ingredientFilter;
		this.registeredIngredients = registeredIngredients;
		this.ingredientManager = ingredientManager;
		this.ingredientVisibility = ingredientVisibility;
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public <T> ITypedIngredient<T> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient) {
		Optional<ITypedIngredient<T>> result = TypedIngredient.createTyped(registeredIngredients, ingredientType, ingredient);
		if (result.isEmpty()) {
			String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient, ingredientType, registeredIngredients);
			throw new IllegalArgumentException("Invalid ingredient: " + ingredientInfo);
		}
		return result.get();
	}

	@Override
	public IRecipeManager getRecipeManager() {
		return recipeManager;
	}

	@Override
	public IIngredientFilter getIngredientFilter() {
		return ingredientFilter;
	}

	@Override
	public IIngredientListOverlay getIngredientListOverlay() {
		return ingredientListOverlay;
	}

	@Override
	public IIngredientManager getIngredientManager() {
		return ingredientManager;
	}

	@Override
	public IBookmarkOverlay getBookmarkOverlay() {
		return bookmarkOverlay;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IRecipesGui getRecipesGui() {
		return recipesGui;
	}

	@Override
	public IIngredientVisibility getIngredientVisibility() {
		return ingredientVisibility;
	}
}
