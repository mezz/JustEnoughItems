package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * A scrolling area for ingredients with a scrollbar.
 * Modeled after the vanilla creative menu.
 *
 * Create one with {@link IRecipeExtrasBuilder#addScrollGridWidget}.
 * @since 15.20.0
 */
@ApiStatus.NonExtendable
public interface IScrollGridWidget extends ISlottedRecipeWidget, IPlaceable<IScrollGridWidget> {
	@Override
	IScrollGridWidget setPosition(int xPos, int yPos);

	@Override
	IScrollGridWidget setPosition(
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
	Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY);

	@Override
	ScreenRectangle getScreenRectangle();
}
