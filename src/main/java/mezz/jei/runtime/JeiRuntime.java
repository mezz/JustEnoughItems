package mezz.jei.runtime;

import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.recipes.RecipeManager;

public class JeiRuntime implements IJeiRuntime {

	private final RecipeManager recipeManager;
	private final IngredientListOverlay ingredientListOverlay;
	private final IBookmarkOverlay bookmarkOverlay;
	private final RecipesGui recipesGui;
	private final IIngredientFilter ingredientFilter;
	private final IIngredientManager ingredientManager;

	public JeiRuntime(RecipeManager recipeManager, IngredientListOverlay ingredientListOverlay, IBookmarkOverlay bookmarkOverlay, RecipesGui recipesGui, IIngredientFilter ingredientFilter, IIngredientManager ingredientManager) {
		this.recipeManager = recipeManager;
		this.ingredientListOverlay = ingredientListOverlay;
		this.bookmarkOverlay = bookmarkOverlay;
		this.recipesGui = recipesGui;
		this.ingredientFilter = ingredientFilter;
		this.ingredientManager = ingredientManager;
	}

	public void close() {
		this.recipesGui.func_231175_as__();
	}

	@Override
	public RecipeManager getRecipeManager() {
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
	public RecipesGui getRecipesGui() {
		return recipesGui;
	}
}
