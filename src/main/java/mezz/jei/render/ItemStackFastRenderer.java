package mezz.jei.render;

import javax.annotation.Nullable;

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

	@Nullable
	private BakedModel getBakedModel() {
		ItemModelShaper itemModelMesher = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
		ItemStack itemStack = element.getIngredient();
		BakedModel bakedModel = itemModelMesher.getItemModel(itemStack);
		//TODO - 1.17: Validate
		return bakedModel.getOverrides().resolve(bakedModel, itemStack, null, null, 0);
	}

	private void uncheckedRenderItemAndEffectIntoGUI(MultiBufferSource buffer, PoseStack poseStack, IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(poseStack, area, padding, editModeConfig);
			RenderSystem.enableBlend();
		}

		ItemStack itemStack = element.getIngredient();
		BakedModel bakedModel = getBakedModel();
		if (bakedModel == null) {
			return;
		}

		poseStack.pushPose();
		poseStack.translate(area.getX() + padding + 16, area.getY() + padding, 150);
		poseStack.scale(16, -16, 16);
		poseStack.translate(-0.5, -0.5, -0.5);
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
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

	private void renderOverlay(ItemStack itemStack, Rect2i area, int padding) {
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
