package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Used to add tooltips to ingredients drawn on a recipe.
 *
 * Implement a tooltip callback and add it with
 * {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)}
 *
 * @since 9.3.0
 */
@FunctionalInterface
public interface IRecipeSlotTooltipCallback {
	/**
	 * Change the tooltip for an ingredient.
	 *
	 * @since 9.3.0
	 */
	void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip);
}
