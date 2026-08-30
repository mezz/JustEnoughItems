package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;

/**
 * Used to add rich tooltips to recipe slots.
 *
 * Implement a tooltip callback and add it with
 * {@link IRecipeSlotBuilder#addRichTooltipCallback(IRecipeSlotRichTooltipCallback)}
 *
 * @since 11.34.3
 */
@FunctionalInterface
public interface IRecipeSlotRichTooltipCallback {
	/**
	 * Add to the tooltip for a recipe slot.
	 *
	 * This is also called when the slot has no displayed ingredient. This allows a slot background,
	 * overlay, or placeholder to provide its own tooltip.
	 *
	 * @since 11.34.3
	 */
	void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip);
}
