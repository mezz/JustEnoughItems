package mezz.jei.api.gui.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.runtime.util.IImmutableRect2i;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A drawable recipe slot, useful if you need to manually draw a slot somewhere.
 *
 * @since 11.5.0
 */
public interface IRecipeSlotDrawable extends IRecipeSlotView {
	/**
	 * Get the position of the recipe slot drawable relative to its parent element.
	 *
	 * @since 11.5.0
	 */
	IImmutableRect2i getRect();

	/**
	 * Draws the recipe slot relative to the pose stack.
	 *
	 * @since 11.5.0
	 */
	void draw(PoseStack poseStack);

	/**
	 * Draws the recipe slot overlays, called when the mouse is hovering over this recipe slot.
	 *
	 * @since 11.5.0
	 */
	void drawHoverOverlays(PoseStack poseStack);

	/**
	 * Get the tooltip for this recipe slot.
	 *
	 * @since 11.5.0
	 */
	List<Component> getTooltip();

	/**
	 * Add a tooltip callback to be called when the mouse is hovering over this recipe slot.
	 *
	 * @since 11.5.0
	 */
	void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback);
}
