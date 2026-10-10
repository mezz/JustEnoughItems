package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.placement.VerticalAlignment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;

/**
 * A scrolling area for ingredients with a scrollbar.
 *
 * Create one with {@link IRecipeExtrasBuilder#addScrollGridWidget}.
 * @since 19.19.3
 */
@ApiStatus.NonExtendable
public interface IScrollGridWidget extends ISlottedRecipeWidget, IPlaceable<IScrollGridWidget> {
	/**
	 * Replace the slots displayed in this grid and scroll back to the top.
	 * The list is copied, and any active scrollbar drag is canceled.
	 *
	 * The grid takes over drawing and hit-testing for slots supplied from its recipe layout.
	 * Slots removed from the list are no longer displayed. This does not change the ingredients
	 * registered for recipe lookup; register all searchable ingredients when building the recipe.
	 *
	 * @since 31.10.0
	 */
	IScrollGridWidget setSlots(List<IRecipeSlotDrawable> slots);

	/**
	 * Set when the scrollbar is shown. Defaults to {@link ScrollbarVisibility#DEFAULT}.
	 * Hiding the scrollbar reduces the grid's width without changing its columns or slot positions
	 * within the grid.
	 * Alignment set with {@link #setPosition(int, int, int, int, HorizontalAlignment, VerticalAlignment)}
	 * is preserved as the width changes.
	 *
	 * @since 31.10.0
	 */
	IScrollGridWidget setScrollbarVisibility(ScrollbarVisibility visibility);

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
