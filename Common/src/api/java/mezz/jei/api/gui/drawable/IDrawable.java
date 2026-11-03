package mezz.jei.api.gui.drawable;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Represents something to be drawn on screen.
 *
 * Useful for drawing miscellaneous things like in
 * {@link IRecipeCategory#draw(Object, IRecipeSlotsView, GuiGraphicsExtractor, double, double)}.
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

	default void draw(GuiGraphicsExtractor guiGraphics) {
		draw(guiGraphics, 0, 0);
	}

	void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset);

	/**
	 * Draws at the given offsets in the given ingredient rendering context.
	 * The context controls whether ingredient drawables render decoration overlays and registered decorators.
	 * Drawables that contain other drawables must forward the context to their children.
	 *
	 * @since 29.54.0
	 */
	default void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, IIngredientRenderContext context) {
		draw(guiGraphics, xOffset, yOffset);
	}

}
