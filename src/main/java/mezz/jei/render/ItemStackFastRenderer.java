package mezz.jei.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.ErrorUtil;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.client.RenderProperties;

public class ItemStackFastRenderer extends IngredientListElementRenderer<ItemStack> {

	public ItemStackFastRenderer(IIngredientListElement<ItemStack> itemStackElement) {
		super(itemStackElement);
	}

	public void renderItemAndEffectIntoGUI(MultiBufferSource buffer, PoseStack poseStack, IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		try {
			uncheckedRenderItemAndEffectIntoGUI(buffer, poseStack, editModeConfig, worldConfig);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	private void uncheckedRenderItemAndEffectIntoGUI(MultiBufferSource buffer, PoseStack poseStack, IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(poseStack, area, padding, editModeConfig);
			RenderSystem.enableBlend();
		}

		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		ItemStack itemStack = element.getIngredient();
		BakedModel bakedModel = itemRenderer.getModel(itemStack, null, null, 0);
		poseStack.pushPose();
		poseStack.translate((area.getX() + padding) / 16D, (area.getY() + padding) / -16D, 0);
		itemRenderer.render(itemStack, ItemTransforms.TransformType.GUI, false, poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
		poseStack.popPose();
	}

	public void renderOverlay() {
		ItemStack itemStack = element.getIngredient();
		try {
			renderOverlay(itemStack, area, padding);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	private static void renderOverlay(ItemStack itemStack, Rect2i area, int padding) {
		Font font = getFontRenderer(itemStack);
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		itemRenderer.renderGuiItemDecorations(font, itemStack, area.getX() + padding, area.getY() + padding, null);
	}

	public static Font getFontRenderer(ItemStack itemStack) {
		Font fontRenderer = RenderProperties.get(itemStack).getFont(itemStack);
		if (fontRenderer == null) {
			fontRenderer = Minecraft.getInstance().font;
		}
		return fontRenderer;
	}
}
