package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.builder.ITooltipBuilder;

/**
 * Builds a rich tooltip for a recipe widget builder.
 *
 * The callback is only invoked when the mouse is within the widget's bounds and the mouse is
 * within the recipe category. Bounds use an exclusive maximum, so a widget with a width of
 * {@code 16} contains x coordinates from {@code 0} (inclusive) to {@code 16} (exclusive).
 *
 * @since 29.34.0
 */
@FunctionalInterface
public interface IRecipeWidgetTooltipCallback {
	/**
	 * Add content to the widget's tooltip.
	 *
	 * @param tooltip a builder that supports text and rich tooltip components
	 *
	 * @since 29.34.0
	 */
	void onTooltip(ITooltipBuilder tooltip);
}
