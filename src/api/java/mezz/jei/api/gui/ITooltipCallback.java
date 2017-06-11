package mezz.jei.api.gui;

import java.util.List;

/**
 * Used to add tooltips to ingredients drawn on a recipe.
 * Implement a tooltip callback and set it with {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}.
 * Note that this works on anything that implements {@link IGuiIngredientGroup}, like {@link IGuiItemStackGroup} and {@link IGuiFluidStackGroup}.
 */
@FunctionalInterface
public interface ITooltipCallback<T> {
	/**
	 * Change the tooltip for an ingredient.
	 */
	void onTooltip(int slotIndex, boolean input, T ingredient, List<String> tooltip);
}
