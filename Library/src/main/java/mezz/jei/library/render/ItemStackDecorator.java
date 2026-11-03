package mezz.jei.library.render;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.rendering.IIngredientDecorator;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public class ItemStackDecorator implements IIngredientDecorator<ItemStack> {
	private final IIngredientRenderer<ItemStack> ingredientRenderer;

	public ItemStackDecorator(IIngredientRenderer<ItemStack> ingredientRenderer) {
		this.ingredientRenderer = ingredientRenderer;
	}

	@Override
	public void render(PoseStack poseStack, ItemStack ingredient, int x, int y, IIngredientRenderContext context) {
		var font = ingredientRenderer.getFontRenderer(Minecraft.getInstance(), ingredient);
		var modelView = RenderSystem.getModelViewStack();
		modelView.pushPose();
		try {
			modelView.mulPoseMatrix(poseStack.last().pose());
			RenderSystem.applyModelViewMatrix();
			Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, ingredient, x, y);
		} finally {
			modelView.popPose();
			RenderSystem.applyModelViewMatrix();
		}
	}
}
