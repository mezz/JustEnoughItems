package mezz.jei.runtime;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IIngredientVisibility;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.Focus;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.RegisteredIngredients;
import mezz.jei.ingredients.TypedIngredient;
import mezz.jei.util.ErrorUtil;

import java.util.Optional;

public class JeiRuntime implements IJeiRuntime {

	private final IRecipeManager recipeManager;
	private final IngredientListOverlay ingredientListOverlay;
	private final IBookmarkOverlay bookmarkOverlay;
	private final RecipesGui recipesGui;
	private final IIngredientFilter ingredientFilter;
	private final RegisteredIngredients registeredIngredients;
	private final IIngredientManager ingredientManager;
	private final IIngredientVisibility ingredientVisibility;
	private final IJeiHelpers jeiHelpers;

	public JeiRuntime(
		IRecipeManager recipeManager,
		IngredientListOverlay ingredientListOverlay,
		IBookmarkOverlay bookmarkOverlay,
		RecipesGui recipesGui,
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

	@SuppressWarnings("removal")
	@Override
	public <T> IFocus<T> createFocus(RecipeIngredientRole role, IIngredientType<T> ingredientType, T ingredient) {
		return Focus.createFromApi(registeredIngredients, role, ingredientType, ingredient);
	}

	@Override
	public <T> ITypedIngredient<T> createTypedIngredient(IIngredientType<T> ingredientType, T ingredient) {
		Optional<ITypedIngredient<T>> result = TypedIngredient.createTyped(registeredIngredients, ingredientType, ingredient);
		if (result.isEmpty()) {
			String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient, ingredientType);
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
	public IngredientListOverlay getIngredientListOverlay() {
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
	public RecipesGui getRecipesGui() {
		return recipesGui;
	}

	@Override
	public IIngredientVisibility getIngredientVisibility() {
		return ingredientVisibility;
	}
}
