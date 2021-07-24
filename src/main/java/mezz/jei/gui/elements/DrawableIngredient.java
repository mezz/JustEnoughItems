package mezz.jei.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.renderer.GameRenderer;

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
	public void draw(PoseStack poseStack, int xOffset, int yOffset) {
		RenderSystem.enableDepthTest();
		this.ingredientRenderer.render(poseStack, xOffset, yOffset, ingredient);
		//TODO - 1.17: Replacement?
		//RenderSystem.enableAlphaTest();
		RenderSystem.disableDepthTest();
	}
}
