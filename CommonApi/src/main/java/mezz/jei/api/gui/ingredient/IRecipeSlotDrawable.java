package mezz.jei.api.gui.ingredient;

import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

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
@ApiStatus.NonExtendable
public interface IRecipeSlotDrawable extends IRecipeSlotView {
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
	 * Return true if the mouse is over the slot.
	 *
	 * @param mouseX relative to its parent element.
	 * @param mouseY relative to its parent element.
	 *
	 * @since 15.9.0
	 */
	boolean isMouseOver(double mouseX, double mouseY);

	/**
	 * Move this slot to the given position.
	 * @param x the new x coordinate, relative to its parent element.
	 * @param y the new y coordinate, relative to its parent element.
	 *
	 * @since 15.9.0
	 */
	void setPosition(int x, int y);

	/**
	 * Overrides the currently displayed ingredients.
	 * Set this from {@link IRecipeCategory#onDisplayedIngredientsUpdate} when the currently displayed ingredients change.
	 *
	 * @since 15.12.1
	 */
	IIngredientConsumer createDisplayOverrides();

	/**
	 * Removes any display overrides that were set with {@link #createDisplayOverrides()}.
	 *
	 * @since 15.12.1
	 */
	void clearDisplayOverrides();

	/**
	 * Get the position and size of the recipe slot drawable relative to its parent element.
	 *
	 * @since 11.5.0
	 * @deprecated use {@link #isMouseOver(double, double)} to check if the mouse is over the slot
	 */
	@Deprecated(since = "15.9.0", forRemoval = true)
	Rect2i getRect();

	/**
	 * Add a tooltip callback to be called when the mouse is hovering over this recipe slot.
	 *
	 * @since 11.5.0
	 * @deprecated use {@link IRecipeSlotBuilder#addRichTooltipCallback(IRecipeSlotRichTooltipCallback)} instead, when creating the slot
	 */
	@SuppressWarnings("removal")
	@Deprecated(since = "15.8.4", forRemoval = true)
	default void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {

	}

	/**
	 * Get the area that this recipe slot draws on, including the area covered by its background texture.
	 * Useful for laying out other recipe elements relative to the slot.
	 *
	 * @since 15.20.0
	 */
	Rect2i getAreaIncludingBackground();
}
