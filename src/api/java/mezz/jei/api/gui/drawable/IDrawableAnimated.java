package mezz.jei.api.gui.drawable;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * An animated {@link IDrawable}, useful for showing a gui animation like furnace flames or progress arrows.
 *
 * Useful for drawing miscellaneous things in
 * {@link IRecipeCategory#draw(Object, IRecipeSlotsView, PoseStack, double, double)}.
 *
 * To create an instance, call
 * {@link IGuiHelper#createAnimatedDrawable(IDrawableStatic, int, StartDirection, boolean)}
 *
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
