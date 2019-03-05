package mezz.jei.api.gui.ingredient;

import java.util.List;

import mezz.jei.api.helpers.IGuiHelper;

/**
 * Helps set crafting-grid-style {@link IGuiIngredientGroup}.
 * This places smaller recipes in the grid in a consistent way.
 * Get an instance from {@link IGuiHelper#createCraftingGridHelper(int)}.
 */
public interface ICraftingGridHelper {

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 */
	<T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs);

	/**
	 * Place inputs ingredients onto the crafting grid in a consistent way.
	 */
	<T> void setInputs(IGuiIngredientGroup<T> ingredientGroup, List<List<T>> inputs, int width, int height);

}
