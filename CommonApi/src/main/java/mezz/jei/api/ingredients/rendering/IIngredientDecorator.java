package mezz.jei.api.ingredients.rendering;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Draws decorations after JEI renders ingredients.
 * JEI invokes decorators only when {@link IIngredientRenderContext#renderDecorations()} is enabled.
 *
 * Register from your plugin's {@code registerGuiHandlers} method with
 * {@link IGuiHandlerRegistration#addIngredientDecorator(IIngredientType, IIngredientDecorator)}.
 * Use {@link VanillaTypes#ITEM_STACK} to decorate items.
 *
 * @since 11.81.0
 */
@FunctionalInterface
public interface IIngredientDecorator<T> {
	/**
	 * Draws decorations for one visible ingredient.
	 *
	 * @param poseStack the graphics for drawing decorations, with the same pose used to draw the ingredient
	 * @param ingredient the displayed ingredient. Do not modify this ingredient.
	 * @param x the ingredient's left edge, relative to the current graphics pose
	 * @param y the ingredient's top edge, relative to the current graphics pose
	 * @param context the rendering policy used for this ingredient
	 *
	 * @since 11.81.0
	 */
	void render(PoseStack poseStack, T ingredient, int x, int y, IIngredientRenderContext context);
}
