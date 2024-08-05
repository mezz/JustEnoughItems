package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
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
		return this.ingredientRenderer.getWidth();
	}

	@Override
	public int getHeight() {
		return this.ingredientRenderer.getHeight();
	}

	@Override
	public void draw(PoseStack poseStack) {
		draw(poseStack, 0, 0);
	}

	@Override
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		RenderSystem.enableDepthTest();
		this.ingredientRenderer.render(poseStack, ingredient, xOffset, yOffset);
		RenderSystem.disableDepthTest();
	}
}
