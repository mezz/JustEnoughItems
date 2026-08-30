package mezz.jei.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.renderer.Rect2i;

/**
 * A widget is a drawable element in a recipe layout.
 *
 * Widgets are created for each displayed recipe, which can be useful when you want to
 * efficiently generate and display information that is specific to a recipe.
 * Contrast this with the {@link IRecipeCategory} methods, which all take a recipe parameter and do not store it.
 *
 * Create your widgets in {@link IRecipeCategory#createRecipeExtras}.
 *
 * @since 11.32.0
 */
public interface IRecipeWidget {
	/**
	 * Get the position and size of this widget, relative to its parent element.
	 * @since 11.32.0
	 */
	Rect2i getArea();

	/**
	 * Draw extras or additional info about the recipe, relative to its {@link #getArea()}.
	 * Use the mouse position for things like button highlights.
	 *
	 * @param poseStack       the current {@link PoseStack} for rendering.
	 * @param mouseX          the X position of the mouse, relative to this widget's position.
	 * @param mouseY          the Y position of the mouse, relative to this widget's position.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
	 * @see IRecipeCategory#draw for a similar method that doesn't require a widget.
	 *
	 * @since 11.38.0
	 */
	default void drawWidget(PoseStack poseStack, double mouseX, double mouseY) {
		Rect2i area = getArea();
		draw(poseStack, mouseX + area.getX(), mouseY + area.getY());
	}

	/**
	 * Draw extras or additional info about the recipe, relative to its {@link #getArea()}.
	 * Use the mouse position for things like button highlights.
	 *
	 * @param poseStack       the current {@link PoseStack} for rendering.
	 * @param mouseX          the X position of the mouse, relative to its parent element.
	 * @param mouseY          the Y position of the mouse, relative to its parent element.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
	 * @see IRecipeCategory#draw for a similar method that doesn't require a widget.
	 *
	 * @since 11.32.0
	 * @deprecated use {@link #drawWidget} which uses mouse coordinates relative to the widget's position instead of the parent's position.
	 */
	@Deprecated(since = "11.38.0", forRemoval = true)
	default void draw(PoseStack poseStack, double mouseX, double mouseY) {

	}

	/**
	 * Add extra tooltips for this widget.
	 *
	 * Mouse coordinates are relative to {@link #getArea()}.
	 *
	 * @param mouseX          the X position of the mouse, relative to this widget's position.
	 * @param mouseY          the Y position of the mouse, relative to this widget's position.
	 *
	 * @since 11.38.0
	 */
	default void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {

	}

	/**
	 * Called once per game tick, useful for updating the widget's state in the background.
	 *
	 * @since 11.32.0
	 */
	default void tick() {

	}
}
