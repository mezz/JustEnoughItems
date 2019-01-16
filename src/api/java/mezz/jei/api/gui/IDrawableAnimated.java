package mezz.jei.api.gui;

import net.minecraft.client.Minecraft;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 * An animated {@link IDrawable}, useful for showing a gui animation like furnace flames or progress arrows.
 * Useful for drawing miscellaneous things in {@link IRecipeCategory#drawExtras(Minecraft)} and {@link IRecipeWrapper#drawInfo(Minecraft, int, int, int, int)}.
 * <p>
 * To create an instance, use {@link IGuiHelper#createAnimatedDrawable(IDrawableStatic, int, StartDirection, boolean)}.
 * Internally, these use an {@link ITickTimer} to simulate tick-driven animations.
 */
public interface IDrawableAnimated extends IDrawable {
	/**
	 * The direction that the animation starts from.
	 *
	 * @see IGuiHelper#createAnimatedDrawable(IDrawableStatic, int, StartDirection, boolean)
	 */
	enum StartDirection {
		TOP, BOTTOM, LEFT, RIGHT
	}
}
