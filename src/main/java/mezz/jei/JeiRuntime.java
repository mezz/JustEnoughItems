package mezz.jei;

import javax.annotation.Nonnull;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.gui.ItemListOverlay;

public class JeiRuntime implements IJeiRuntime {

	private final RecipeRegistry recipeRegistry;
	private final ItemListOverlay itemListOverlay;

	public JeiRuntime(RecipeRegistry recipeRegistry, ItemListOverlay itemListOverlay) {
		this.recipeRegistry = recipeRegistry;
		this.itemListOverlay = itemListOverlay;
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
}
