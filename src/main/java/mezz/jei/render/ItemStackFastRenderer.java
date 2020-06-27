package mezz.jei.render;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.ErrorUtil;

public class ItemStackFastRenderer extends IngredientListElementRenderer<ItemStack> {

	public ItemStackFastRenderer(IIngredientListElement<ItemStack> itemStackElement) {
		super(itemStackElement);
	}

	public void renderItemAndEffectIntoGUI(MatrixStack matrixStack, IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		try {
			uncheckedRenderItemAndEffectIntoGUI(matrixStack, editModeConfig, worldConfig);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	@Nullable
	private IBakedModel getBakedModel() {
		ItemModelMesher itemModelMesher = Minecraft.getInstance().getItemRenderer().getItemModelMesher();
		ItemStack itemStack = element.getIngredient();
		IBakedModel bakedModel = itemModelMesher.getItemModel(itemStack);
		return bakedModel.getOverrides().func_239290_a_(bakedModel, itemStack, null, null);
	}

	private void uncheckedRenderItemAndEffectIntoGUI(MatrixStack matrixStack, IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(matrixStack, area, padding, editModeConfig);
			RenderSystem.enableBlend();
		}

		ItemStack itemStack = element.getIngredient();
		IBakedModel bakedModel = getBakedModel();
		if (bakedModel == null) {
			return;
		}

		matrixStack.push();
		matrixStack.translate(area.getX() + padding + 16, area.getY() + padding, 150);
		matrixStack.scale(16, -16, 16);
		matrixStack.translate(-0.5, -0.5, -0.5);
		Minecraft minecraft = Minecraft.getInstance();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		IRenderTypeBuffer.Impl iRenderTypeBuffer = minecraft.getRenderTypeBuffers().getBufferSource();
		itemRenderer.renderItem(itemStack, ItemCameraTransforms.TransformType.GUI, false, matrixStack, iRenderTypeBuffer, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
		iRenderTypeBuffer.finish();
		matrixStack.pop();

	}

	public void renderOverlay() {
		ItemStack itemStack = element.getIngredient();
		try {
			renderOverlay(itemStack, area, padding);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	private void renderOverlay(ItemStack itemStack, Rectangle2d area, int padding) {
		FontRenderer font = getFontRenderer(itemStack);
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		itemRenderer.renderItemOverlayIntoGUI(font, itemStack, area.getX() + padding, area.getY() + padding, null);
	}

	public static FontRenderer getFontRenderer(ItemStack itemStack) {
		Item item = itemStack.getItem();
		FontRenderer fontRenderer = item.getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = Minecraft.getInstance().fontRenderer;
		}
		return fontRenderer;
	}
}
