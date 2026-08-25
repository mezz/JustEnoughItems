package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.ITooltipBuilder;

/**
 * An element in a recipe layout that can append lines to its own tooltip.
 *
 * <p>JEI first fills the tooltip with the standard information for the currently displayed ingredient
 * (mod name, tag name, and user configuration), then calls {@link #addTooltip(ITooltipBuilder)}
 * so the implementation can append any extra lines.</p>
 *
 * @since 30.26.0
 */
public interface IRecipeTooltipAppender {
	/**
	 * Append extra lines to the given tooltip.
	 *
	 * @param tooltip the tooltip builder to append to.
	 *
	 * @since 30.26.0
	 */
	default void addTooltip(ITooltipBuilder tooltip) {
	}
}
