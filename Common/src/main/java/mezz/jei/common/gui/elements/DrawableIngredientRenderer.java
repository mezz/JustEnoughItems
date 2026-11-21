package mezz.jei.common.gui.elements;

import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawableIngredientRenderer<T> implements IDrawable {
	private final IIngredientRenderer<T> ingredientRenderer;
	private final T ingredient;

	public DrawableIngredientRenderer(IIngredientRenderer<T> ingredientRenderer, T ingredient) {
		this.ingredientRenderer = ingredientRenderer;
		this.ingredient = ingredient;
	}

	@Override
	public int getWidth() {
		return ingredientRenderer.getWidth();
	}

	@Override
	public int getHeight() {
		return ingredientRenderer.getHeight();
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void draw(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset, IIngredientRenderContext context) {
		ingredientRenderer.render(guiGraphics, ingredient, xOffset, yOffset, context);
	}
}
