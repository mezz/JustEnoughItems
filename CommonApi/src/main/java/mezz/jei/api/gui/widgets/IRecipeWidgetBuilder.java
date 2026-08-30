package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * Configures a recipe widget created by {@link IRecipeExtrasBuilder}.
 *
 * The widget's position is relative to the recipe category. Its width and height define its
 * automatic tooltip bounds, with an inclusive minimum and exclusive maximum.
 *
 * @param <THIS> the concrete builder interface, for chaining configuration methods
 *
 * @since 11.61.0
 */
@ApiStatus.NonExtendable
public interface IRecipeWidgetBuilder<THIS extends IRecipeWidgetBuilder<THIS>> extends IPlaceable<THIS> {
	/**
	 * Set one line of text as this widget's tooltip.
	 *
	 * @since 11.61.0
	 */
	THIS setTooltip(FormattedText tooltip);

	/**
	 * Set text lines as this widget's tooltip.
	 * The collection is copied when this method is called.
	 *
	 * @since 11.61.0
	 */
	THIS setTooltip(Collection<? extends FormattedText> tooltip);

	/**
	 * Set a rich component as this widget's tooltip.
	 *
	 * @since 11.61.0
	 */
	THIS setTooltip(TooltipComponent tooltip);

	/**
	 * Set a callback that builds this widget's tooltip.
	 * The callback is invoked only while the mouse is within this widget's exclusive bounds and
	 * within the recipe category.
	 *
	 * @since 11.61.0
	 */
	THIS setTooltip(IRecipeWidgetTooltipCallback tooltipCallback);

	@Override
	THIS setPosition(int xPos, int yPos);

	@Override
	THIS setPosition(
		int areaX,
		int areaY,
		int areaWidth,
		int areaHeight,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment
	);

	@Override
	int getWidth();

	@Override
	int getHeight();
}
