package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A drawable recipe slot, useful if you need to make JEI draw a slot somewhere.
 *
 * Created from a {@link IRecipeSlotBuilder}, usually from {@link IRecipeLayoutBuilder#addSlot},
 * using the {@link IRecipeLayoutBuilder} given to mod plugins in {@link IRecipeCategory#setRecipe}.
 *
 * You can also create one for other purposes with {@link IRecipeManager#createRecipeSlotDrawable}.
 *
 * @since 11.5.0
 */
public interface IRecipeSlotDrawable extends IRecipeSlotView {
	/**
	 * Get the position and size of the recipe slot drawable relative to its parent element.
	 *
	 * @since 11.5.0
	 */
	Rect2i getRect();

	/**
	 * Draws the recipe slot relative to the pose stack.
	 *
	 * @since 11.5.0
	 */
	void draw(GuiGraphics guiGraphics);

	/**
	 * Draws the recipe slot overlays, called when the mouse is hovering over this recipe slot.
	 *
	 * @since 11.5.0
	 */
	void drawHoverOverlays(GuiGraphics guiGraphics);

	/**
	 * Get the plain tooltip for this recipe slot.
	 *
	 * @since 11.5.0
	 */
	List<Component> getTooltip();

	/**
	 * Get the rich tooltip for this recipe slot.
	 *
	 * @since 15.8.4
	 */
	void getTooltip(ITooltipBuilder tooltipBuilder);

	/**
	 * Add a tooltip callback to be called when the mouse is hovering over this recipe slot.
	 *
	 * @since 11.5.0
	 * @deprecated use {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)} instead, when creating the slot
	 */
	@Deprecated(since = "15.8.4", forRemoval = true)
	default void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {

	}
}
