package mezz.jei.api.gui.drawable;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * Represents something to be drawn on screen.
 *
 * Useful for drawing miscellaneous things like in
 * {@link IRecipeCategory#draw(Object, IRecipeSlotsView, PoseStack, double, double)}.
 * {@link IRecipeCategory#getBackground()}
 * {@link IRecipeSlotBuilder#setBackground(IDrawable, int, int)}
 * {@link IRecipeSlotBuilder#setOverlay(IDrawable, int, int)}]
 * and anywhere else things are drawn on the screen.
 *
 * @see IGuiHelper for many functions to create IDrawables.
 * @see IGuiHelper#createDrawableIngredient(IIngredientType, Object) to draw an ingredient.
 * @see IDrawableAnimated
 * @see IDrawableStatic
 */
public interface IDrawable {

	int getWidth();

	int getHeight();

	default void draw(PoseStack poseStack) {
		draw(poseStack, 0, 0);
	}

	void draw(PoseStack poseStack, int xOffset, int yOffset);

}
