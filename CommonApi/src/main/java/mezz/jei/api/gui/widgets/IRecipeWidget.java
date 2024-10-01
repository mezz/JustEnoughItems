package mezz.jei.api.gui.widgets;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;

/**
 * A widget is a drawable element in a recipe layout.
 *
 * Widgets are created for each displayed recipe, which can be useful when you want to
 * efficiently generate and display information that is specific to a recipe.
 * Contrast this with the {@link IRecipeCategory} methods, which all take a recipe parameter and do not store it.
 *
 * Create your widgets in {@link IRecipeCategory#createRecipeExtras}.
 *
 * @since 15.10.0
 */
public interface IRecipeWidget {
	/**
	 * Get the position of this widget, relative to its parent element.
	 * @since 15.10.0
	 */
	ScreenPosition getPosition();

	/**
	 * Draw extras or additional info about the recipe, relative to its {@link #getPosition()}.
	 * Use the mouse position for things like button highlights.
	 *
	 * @param guiGraphics     the current {@link GuiGraphics} for rendering.
	 * @param mouseX          the X position of the mouse, relative to its position.
	 * @param mouseY          the Y position of the mouse, relative to its position.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
	 * @see IRecipeCategory#draw for a similar method that doesn't require a widget.
	 *
	 * @since 15.20.0
	 */
	default void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		ScreenPosition position = getPosition();
		draw(guiGraphics, mouseX + position.x(),  mouseY + position.y());
	}

	/**
	 * Draw extras or additional info about the recipe, relative to its {@link #getPosition()}.
	 * Use the mouse position for things like button highlights.
	 *
	 * @param guiGraphics     the current {@link GuiGraphics} for rendering.
	 * @param mouseX          the X position of the mouse, relative to its parent element.
	 * @param mouseY          the Y position of the mouse, relative to its parent element.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
	 * @see IRecipeCategory#draw for a similar method that doesn't require a widget.
	 *
	 * @since 15.10.0
	 * @deprecated use {@link #drawWidget} which uses mouse coordinates relative to the widget's position instead of the parent's position.
	 */
	@Deprecated(since = "15.20.0", forRemoval = true)
	default void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {

	}

	/**
	 * Add extra tooltips for this widget.
	 *
	 * Be careful to only add tooltips when the mouse is over the widget,
	 * there is no way to determine if the mouse is over this widget except in this method.
	 *
	 * @param mouseX          the X position of the mouse, relative to its position.
	 * @param mouseY          the Y position of the mouse, relative to its position.
	 *
	 * @since 15.20.0
	 */
	default void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {

	}

	/**
	 * Called once per game tick, useful for updating the widget's state in the background.
	 *
	 * @since 15.10.0
	 */
	default void tick() {

	}
}
