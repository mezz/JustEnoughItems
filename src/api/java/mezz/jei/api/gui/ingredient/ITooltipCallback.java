package mezz.jei.api.gui.ingredient;

import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Used to add tooltips to ingredients drawn on a recipe.
 * Implement a tooltip callback and set it with {@link IGuiIngredientGroup#addTooltipCallback(ITooltipCallback)}.
 *
 * Note that this works on anything that implements {@link IGuiIngredientGroup},
 * like {@link IGuiItemStackGroup} and {@link IGuiFluidStackGroup}.
 *
 * @deprecated since JEI 9.3.0. Use {@link IRecipeSlotTooltipCallback} instead.
 */
@Deprecated
@FunctionalInterface
public interface ITooltipCallback<T> {
	/**
	 * Change the tooltip for an ingredient.
	 * @deprecated since JEI 9.3.0. Use {@link IRecipeSlotTooltipCallback#onTooltip(IGuiIngredient, List)} instead.
	 */
	@Deprecated
	void onTooltip(int slotIndex, boolean input, T ingredient, List<Component> tooltip);
}
