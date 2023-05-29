package mezz.jei.common.gui.elements;

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
	public void draw(GuiGraphics guiGraphics) {
		RenderSystem.enableDepthTest();
		this.ingredientRenderer.render(guiGraphics, ingredient);
		RenderSystem.disableDepthTest();
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		var poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(xOffset, yOffset, 0);
			draw(guiGraphics);
		}
		poseStack.popPose();
	}
}
