package mezz.jei.gui.elements;

import net.minecraft.client.renderer.GlStateManager;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;

public class DrawableIngredient<V> implements IDrawable {
	private final V ingredient;
	private final IIngredientRenderer<V> ingredientRenderer;

	public DrawableIngredient(V ingredient, IIngredientRenderer<V> ingredientRenderer) {
		this.ingredient = ingredient;
		this.ingredientRenderer = ingredientRenderer;
	}

	@Override
	public int getWidth() {
		return 16;
	}

	@Override
	public int getHeight() {
		return 16;
	}

	@Override
	public void draw(int xOffset, int yOffset) {
		GlStateManager.enableDepthTest();
		this.ingredientRenderer.render(xOffset, yOffset, ingredient);
		GlStateManager.enableAlphaTest();
		GlStateManager.disableDepthTest();
	}
}
