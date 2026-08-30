package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

/**
 * Configures a recipe widget backed by an {@link IDrawable}.
 *
 * Its dimensions come from the drawable, including padding configured with
 * {@link IDrawableBuilder#addPadding(int, int, int, int)}.
 *
 * @since 19.51.0
 */
@ApiStatus.NonExtendable
public interface IDrawableWidget extends IRecipeWidgetBuilder<IDrawableWidget> {
	@Override
	IDrawableWidget setPosition(int xPos, int yPos);

	@Override
	IDrawableWidget setPosition(
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

	@Override
	IDrawableWidget setTooltip(FormattedText tooltip);

	@Override
	IDrawableWidget setTooltip(Collection<? extends FormattedText> tooltip);

	@Override
	IDrawableWidget setTooltip(TooltipComponent tooltip);

	@Override
	IDrawableWidget setTooltip(IRecipeWidgetTooltipCallback tooltipCallback);
}
