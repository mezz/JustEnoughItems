package mezz.jei.runtime;

import mezz.jei.api.IIngredientFilter;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.recipes.RecipeRegistry;

public class JeiRuntime implements IJeiRuntime {

	private final RecipeRegistry recipeRegistry;
	private final IngredientListOverlay ingredientListOverlay;
	private final RecipesGui recipesGui;
	private final IIngredientFilter ingredientFilter;

	public JeiRuntime(RecipeRegistry recipeRegistry, IngredientListOverlay ingredientListOverlay, RecipesGui recipesGui, IIngredientFilter ingredientFilter) {
		this.recipeRegistry = recipeRegistry;
		this.ingredientListOverlay = ingredientListOverlay;
		this.recipesGui = recipesGui;
		this.ingredientFilter = ingredientFilter;
	}

	public void close() {
		this.recipesGui.close();
	}

	@Override
	public RecipeRegistry getRecipeRegistry() {
		return recipeRegistry;
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
	public RecipesGui getRecipesGui() {
		return recipesGui;
	}
}
