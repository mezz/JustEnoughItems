package mezz.jei.api.gui.widgets;

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
 * @since 19.7.0
 */
public interface IRecipeWidget {
	/**
	 * Get the position of this widget, relative to its parent element.
	 * @since 19.7.0
	 */
	ScreenPosition getPosition();

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
	 * @since 19.7.0
	 */
	default void draw(GuiGraphics guiGraphics, double mouseX, double mouseY) {

	}

	/**
	 * Called once per game tick, useful for updating the widget's state in the background.
	 *
	 * @since 19.7.0
	 */
	default void tick() {

	}
}
