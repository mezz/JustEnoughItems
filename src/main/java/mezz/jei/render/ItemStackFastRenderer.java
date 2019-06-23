package mezz.jei.render;

import javax.annotation.Nullable;

import net.minecraftforge.client.ForgeHooksClient;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.ErrorUtil;

public class ItemStackFastRenderer extends IngredientListElementRenderer<ItemStack> {
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	public ItemStackFastRenderer(IIngredientListElement<ItemStack> itemStackElement) {
		super(itemStackElement);
	}

	public void renderItemAndEffectIntoGUI(IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		try {
			uncheckedRenderItemAndEffectIntoGUI(editModeConfig, worldConfig);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, element.getIngredient());
		}
	}

	@Nullable
	private IBakedModel getBakedModel() {
		ItemModelMesher itemModelMesher = Minecraft.getInstance().getItemRenderer().getItemModelMesher();
		ItemStack itemStack = element.getIngredient();
		IBakedModel bakedModel = itemModelMesher.getItemModel(itemStack);
		return bakedModel.getOverrides().getModelWithOverrides(bakedModel, itemStack, null, null);
	}

	private void uncheckedRenderItemAndEffectIntoGUI(IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(area, padding, editModeConfig);
			GlStateManager.enableBlend();
		}

		ItemStack itemStack = element.getIngredient();
		IBakedModel bakedModel = getBakedModel();
		if (bakedModel == null) {
			return;
		}

		GlStateManager.pushMatrix();
		{
			GlStateManager.translatef(area.getX() + padding + 8.0f, area.getY() + padding + 8.0f, 150.0F);
			GlStateManager.scalef(16F, -16F, 16F);
			bakedModel = ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GUI, false);
			GlStateManager.translatef(-0.5F, -0.5F, -0.5F);

			Minecraft minecraft = Minecraft.getInstance();
			ItemRenderer itemRenderer = minecraft.getItemRenderer();
			itemRenderer.renderModel(bakedModel, itemStack);

			if (itemStack.hasEffect()) {
				renderEffect(bakedModel);
			}
		}
		GlStateManager.popMatrix();
	}

	protected void renderEffect(IBakedModel model) {
		Minecraft minecraft = Minecraft.getInstance();
		TextureManager textureManager = minecraft.getTextureManager();
		ItemRenderer itemRenderer = minecraft.getItemRenderer();

		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(514);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
		textureManager.bindTexture(RES_ITEM_GLINT);
		GlStateManager.matrixMode(5890);

		GlStateManager.pushMatrix();
		GlStateManager.scalef(8.0F, 8.0F, 8.0F);
		float f = (float) (Util.milliTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translatef(f, 0.0F, 0.0F);
		GlStateManager.rotatef(-50.0F, 0.0F, 0.0F, 1.0F);
		itemRenderer.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scalef(8.0F, 8.0F, 8.0F);
		float f1 = (float) (Util.milliTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translatef(-f1, 0.0F, 0.0F);
		GlStateManager.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
		itemRenderer.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
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
