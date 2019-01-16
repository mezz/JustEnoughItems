package mezz.jei.api.gui;

import net.minecraft.client.Minecraft;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 * Represents something to be drawn on screen.
 * Useful for drawing miscellaneous things in {@link IRecipeCategory#drawExtras(Minecraft)} and {@link IRecipeWrapper#drawInfo(Minecraft, int, int, int, int)}.
 * {@link IGuiHelper} has many functions to create IDrawables.
 *
 * @see IDrawableAnimated
 * @see IDrawableStatic
 */
public interface IDrawable {

	int getWidth();

	int getHeight();

	default void draw(Minecraft minecraft) {
		draw(minecraft, 0, 0);
	}

	void draw(Minecraft minecraft, int xOffset, int yOffset);

}
