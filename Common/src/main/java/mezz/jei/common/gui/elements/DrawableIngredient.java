package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.client.gui.GuiGraphics;

public class DrawableIngredient<V> implements IDrawable {
	private final ITypedIngredient<V> typedIngredient;
	private final IIngredientRenderer<V> ingredientRenderer;

	public DrawableIngredient(ITypedIngredient<V> typedIngredient, IIngredientRenderer<V> ingredientRenderer) {
		this.typedIngredient = typedIngredient;
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
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		draw(guiGraphics, xOffset, yOffset, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, IIngredientRenderContext context) {
		RenderSystem.enableDepthTest();
		try {
			SafeIngredientUtil.render(guiGraphics, ingredientRenderer, typedIngredient, xOffset, yOffset, context);
		} finally {
			RenderSystem.disableDepthTest();
		}
	}
}
