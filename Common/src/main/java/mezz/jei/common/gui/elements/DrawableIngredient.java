package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphics;

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
	public void draw(GuiGraphics poseStack) {
		RenderSystem.enableDepthTest();
		this.ingredientRenderer.render(poseStack, ingredient);
		RenderSystem.disableDepthTest();
	}

	@Override
	public void draw(GuiGraphics poseStack, int xOffset, int yOffset) {
		poseStack.pose().pushPose();
		{
			poseStack.pose().translate(xOffset, yOffset, 0);
			draw(poseStack);
		}
		poseStack.pose().popPose();
	}
}