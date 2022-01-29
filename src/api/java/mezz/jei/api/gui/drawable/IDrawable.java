package mezz.jei.api.gui.drawable;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * Represents something to be drawn on screen.
 * Useful for drawing miscellaneous things in
 * {@link IRecipeCategory#draw(Object, IRecipeLayoutView, PoseStack, double, double)}.
 *
 * @see IGuiHelper for many functions to create IDrawables.
 * @see IDrawableAnimated
 * @see IDrawableStatic
 */
public interface IDrawable {

	int getWidth();

	int getHeight();

	default void draw(PoseStack stack) {
		draw(stack, 0, 0);
	}

	void draw(PoseStack stack, int xOffset, int yOffset);

}
