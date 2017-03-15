package mezz.jei.runtime;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.gui.overlay.ItemListOverlay;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.recipes.RecipeRegistry;
import net.minecraft.client.gui.inventory.GuiContainer;

public class JeiRuntime implements IJeiRuntime {

	private final RecipeRegistry recipeRegistry;
	private final ItemListOverlay itemListOverlay;
	private final RecipesGui recipesGui;
	private final IngredientRegistry ingredientRegistry;
	private final List<IAdvancedGuiHandler<?>> advancedGuiHandlers;

	public JeiRuntime(RecipeRegistry recipeRegistry, ItemListOverlay itemListOverlay, RecipesGui recipesGui, IngredientRegistry ingredientRegistry, List<IAdvancedGuiHandler<?>> advancedGuiHandlers) {
		this.recipeRegistry = recipeRegistry;
		this.itemListOverlay = itemListOverlay;
		this.recipesGui = recipesGui;
		this.ingredientRegistry = ingredientRegistry;
		this.advancedGuiHandlers = advancedGuiHandlers;
	}

	public void close() {
		this.recipesGui.close();
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
	public RecipesGui getRecipesGui() {
		return recipesGui;
	}

	public IngredientRegistry getIngredientRegistry() {
		return ingredientRegistry;
	}

	public <T extends GuiContainer> List<IAdvancedGuiHandler<T>> getActiveAdvancedGuiHandlers(T guiContainer) {
		List<IAdvancedGuiHandler<T>> activeAdvancedGuiHandler = new ArrayList<IAdvancedGuiHandler<T>>();
		for (IAdvancedGuiHandler<?> advancedGuiHandler : advancedGuiHandlers) {
			Class<?> guiContainerClass = advancedGuiHandler.getGuiContainerClass();
			if (guiContainerClass.isInstance(guiContainer)) {
				//noinspection unchecked
				activeAdvancedGuiHandler.add((IAdvancedGuiHandler<T>) advancedGuiHandler);
			}
		}
		return activeAdvancedGuiHandler;
	}

}
