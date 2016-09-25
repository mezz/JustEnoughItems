package mezz.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IRecipesGui;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;

public class JeiRuntime implements IJeiRuntime {

	private final RecipeRegistry recipeRegistry;
	private final ItemListOverlay itemListOverlay;
	private final RecipesGui recipesGui;
	private final IngredientRegistry ingredientRegistry;

	public JeiRuntime(RecipeRegistry recipeRegistry, ItemListOverlay itemListOverlay, RecipesGui recipesGui, IngredientRegistry ingredientRegistry) {
		this.recipeRegistry = recipeRegistry;
		this.itemListOverlay = itemListOverlay;
		this.recipesGui = recipesGui;
		this.ingredientRegistry = ingredientRegistry;
	}

	public void close() {
		if (itemListOverlay.isOpen()) {
			itemListOverlay.close();
		}
		if (recipesGui.isOpen()) {
			recipesGui.close();
		}
	}

	@Override
	public RecipeRegistry getRecipeRegistry() {
		return recipeRegistry;
	}

	@Override
	public ItemListOverlay getItemListOverlay() {
		return itemListOverlay;
	}

	@Override
	public IRecipesGui getRecipesGui() {
		return recipesGui;
	}

	public IngredientRegistry getIngredientRegistry() {
		return ingredientRegistry;
	}
}
