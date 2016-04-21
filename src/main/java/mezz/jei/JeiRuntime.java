package mezz.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IRecipesGui;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;

import javax.annotation.Nonnull;

public class JeiRuntime implements IJeiRuntime {

	private final RecipeRegistry recipeRegistry;
	private final ItemListOverlay itemListOverlay;
	private final RecipesGui recipesGui;

	public JeiRuntime(RecipeRegistry recipeRegistry, ItemListOverlay itemListOverlay, RecipesGui recipesGui) {
		this.recipeRegistry = recipeRegistry;
		this.itemListOverlay = itemListOverlay;
		this.recipesGui = recipesGui;
	}

	public void close() {
		if (itemListOverlay.isOpen()) {
			itemListOverlay.close();
		}
		if (recipesGui.isOpen()) {
			recipesGui.close();
		}
	}

	@Nonnull
	@Override
	public RecipeRegistry getRecipeRegistry() {
		return recipeRegistry;
	}

	@Nonnull
	@Override
	public ItemListOverlay getItemListOverlay() {
		return itemListOverlay;
	}

	@Nonnull
	@Override
	public IRecipesGui getRecipesGui() {
		return recipesGui;
	}
}
