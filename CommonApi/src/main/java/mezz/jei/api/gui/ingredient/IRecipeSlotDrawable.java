package mezz.jei.api.gui.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
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
	 * @deprecated use {@link #draw(PoseStack, boolean)}
	 */
	@Deprecated(since = "11.20.0", forRemoval = true)
	void draw(PoseStack poseStack);

	/**
	 * Draws the recipe slot relative to the pose stack.
	 *
	 * @since 11.20.0
	 */
	void draw(PoseStack poseStack, boolean hovered);

	/**
	 * Draws the recipe slot overlays, called when the mouse is hovering over this recipe slot.
	 *
	 * @since 11.5.0
	 * @deprecated use {@link #draw(PoseStack, boolean)}
	 */
	@Deprecated(since = "11.20.0", forRemoval = true)
	void drawHoverOverlays(PoseStack poseStack);

	/**
	 * Get the plain tooltip for this recipe slot.
	 *
	 * @since 11.5.0
	 */
	List<Component> getTooltip();

	/**
	 * Get the rich tooltip for this recipe slot.
	 *
	 * @since 11.30.1
	 */
	void getTooltip(ITooltipBuilder tooltipBuilder);

	/**
	 * Return true if the mouse is over the slot.
	 *
	 * @param mouseX relative to its parent element.
	 * @param mouseY relative to its parent element.
	 *
	 * @since 11.7.0
	 */
	boolean isMouseOver(double mouseX, double mouseY);

	/**
	 * Move this slot to the given position.
	 * @param x the new x coordinate, relative to its parent element.
	 * @param y the new y coordinate, relative to its parent element.
	 *
	 * @since 11.31.0
	 */
	void setPosition(int x, int y);

	/**
	 * Overrides the currently displayed ingredients.
	 * Set this from {@link IRecipeCategory#onDisplayedIngredientsUpdate} when the currently displayed ingredients change.
	 *
	 * @since 11.34.1
	 */
	IIngredientConsumer createDisplayOverrides();

	/**
	 * Removes any display overrides that were set with {@link #createDisplayOverrides()}.
	 *
	 * @since 11.34.1
	 */
	void clearDisplayOverrides();

	/**
	 * Get the position and size of the recipe slot drawable relative to its parent element.
	 *
	 * @since 11.5.0
	 * @deprecated use {@link #isMouseOver(double, double)} to check if the mouse is over the slot
	 */
	@Deprecated(since = "11.31.0", forRemoval = true)
	Rect2i getRect();

	/**
	 * Add a tooltip callback to be called when the mouse is hovering over this recipe slot.
	 *
	 * @since 11.5.0
	 * @deprecated use {@link IRecipeSlotBuilder#addRichTooltipCallback(IRecipeSlotRichTooltipCallback)} instead, when creating the slot
	 */
	@Deprecated(since = "11.30.1", forRemoval = true)
	void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback);

	/**
	 * Get the area that this recipe slot draws on, including the area covered by its background texture.
	 * Useful for laying out other recipe elements relative to the slot.
	 *
	 * @since 11.38.3
	 */
	Rect2i getAreaIncludingBackground();
}
