package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.pipeline.LightUtil;

import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;

@SuppressWarnings("deprecation")
public class GuiItemStackFast {
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	private final int xPosition;
	private final int yPosition;
	private final int width;
	private final int height;
	private final int padding;
	private final ItemModelMesher itemModelMesher;

	private ItemStack itemStack;

	public GuiItemStackFast(int xPosition, int yPosition, int padding) {
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.padding = padding;
		this.width = 16 + (2 * padding);
		this.height = 16 + (2 * padding);
		this.itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void clear() {
		this.itemStack = null;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return (itemStack != null) && (mouseX >= xPosition) && (mouseY >= yPosition) && (mouseX < xPosition + width) && (mouseY < yPosition + height);
	}

	public void renderItemAndEffectIntoGUI() {
		if (itemStack == null) {
			return;
		}

		IBakedModel bakedModel = itemModelMesher.getItemModel(itemStack);

		GlStateManager.pushMatrix();

		int x = xPosition + padding + 8;
		int y = yPosition + padding + 8;

		if (bakedModel.isGui3d()) {
			if (Config.isEditModeEnabled()) {
				GlStateManager.scale(1.0/20.0F, 1.0/20.0F, 1.0/-20.0F);
				renderEditMode();
				GlStateManager.enableBlend();
				GlStateManager.scale(20.0F, 20.0F, -20.0F);
			}

			GlStateManager.translate(((float) x) / 20f, ((float) y) / 20f, (100.0F + 50f) / -20f);
			GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		} else {
			if (Config.isEditModeEnabled()) {
				GlStateManager.scale(1.0/32.0F, 1.0/32.0F, 1.0/-32.0F);
				renderEditMode();
				GlStateManager.enableBlend();
				GlStateManager.scale(32.0F, 32.0F, -32.0F);
			}

			GlStateManager.translate(((float) x) / 32f, ((float) y) / 32f, (100.0F + 50f) / -32f);
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
		}

		bakedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GUI);

		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);

		renderModel(bakedModel, itemStack);

		if (itemStack.hasEffect()) {
			renderEffect(bakedModel);
		}

		GlStateManager.popMatrix();
	}

	private void renderModel(IBakedModel model, ItemStack stack) {
		this.renderModel(model, -1, stack);
	}

	private void renderModel(IBakedModel model, int color, ItemStack stack) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.startDrawingQuads();
		worldrenderer.setVertexFormat(DefaultVertexFormats.ITEM);

		for (EnumFacing enumfacing : EnumFacing.VALUES) {
			this.renderQuads(worldrenderer, model.getFaceQuads(enumfacing), color, stack);
		}

		this.renderQuads(worldrenderer, model.getGeneralQuads(), color, stack);
		tessellator.draw();
	}

	private void renderQuads(WorldRenderer renderer, List quads, int color, ItemStack stack) {
		boolean flag = color == -1 && stack != null;
		BakedQuad bakedquad;
		int j;

		for (Object quad : quads) {
			bakedquad = (BakedQuad) quad;
			j = color;

			if (flag && bakedquad.hasTintIndex()) {
				j = stack.getItem().getColorFromItemStack(stack, bakedquad.getTintIndex());

				if (EntityRenderer.anaglyphEnable) {
					j = TextureUtil.anaglyphColor(j);
				}

				j |= -16777216;
			}
			LightUtil.renderQuadColor(renderer, bakedquad, j);
		}
	}

	private void renderEffect(IBakedModel model) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(514);
		GlStateManager.blendFunc(768, 1);
		textureManager.bindTexture(RES_ITEM_GLINT);
		GlStateManager.matrixMode(5890);

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translate(f, 0.0F, 0.0F);
		GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
		this.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translate(-f1, 0.0F, 0.0F);
		GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
		this.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
	}

	private void renderModel(IBakedModel model, int color) {
		this.renderModel(model, color, null);
	}

	public void renderSlow() {
		if (Config.isEditModeEnabled()) {
			renderEditMode();
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		FontRenderer font = getFontRenderer(minecraft, itemStack);
		RenderItem renderItem = minecraft.getRenderItem();
		renderItem.renderItemAndEffectIntoGUI(itemStack, xPosition + padding, yPosition + padding);
		renderItem.renderItemOverlayIntoGUI(font, itemStack, xPosition + padding, yPosition + padding, null);
	}

	private void renderEditMode() {
		if (Config.isItemOnConfigBlacklist(itemStack, false)) {
			GuiScreen.drawRect(xPosition + padding, yPosition + padding, xPosition + 8 + padding, yPosition + 16 + padding, 0xFFFFFF00);
		}
		if (Config.isItemOnConfigBlacklist(itemStack, true)) {
			GuiScreen.drawRect(xPosition + 8 + padding, yPosition + padding, xPosition + 16 + padding, yPosition + 16 + padding, 0xFFFF0000);
		}
	}

	public FontRenderer getFontRenderer(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}

	public void drawHovered(Minecraft minecraft, int mouseX, int mouseY) {
		try {
			Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + width, 0x7FFFFFFF);

			renderSlow();

			List<String> tooltip = getTooltip(minecraft, itemStack);
			FontRenderer fontRenderer = getFontRenderer(minecraft, itemStack);
			TooltipRenderer.drawHoveringText(minecraft, tooltip, mouseX, mouseY, fontRenderer);
		} catch (RuntimeException e) {
			Log.error("Exception when rendering tooltip on {}.", itemStack, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getTooltip(@Nonnull Minecraft minecraft, @Nonnull ItemStack itemStack) {
		List<String> list = itemStack.getTooltip(minecraft.thePlayer, minecraft.gameSettings.advancedItemTooltips);
		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, itemStack.getRarity().rarityColor + list.get(k));
			} else {
				list.set(k, EnumChatFormatting.GRAY + list.get(k));
			}
		}

		if (Config.isEditModeEnabled()) {
			list.add("");
			list.add(EnumChatFormatting.ITALIC + Translator.translateToLocal("gui.jei.editMode.description"));
			if (Config.isItemOnConfigBlacklist(itemStack, false)) {
				String description = EnumChatFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.show");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
			} else {
				String description = EnumChatFormatting.YELLOW + Translator.translateToLocal("gui.jei.editMode.description.hide");
				list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
			}

			Item item = itemStack.getItem();
			if (item.getHasSubtypes()) {
				if (Config.isItemOnConfigBlacklist(itemStack, true)) {
					String description = EnumChatFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.show.wild");
					list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
				} else {
					String description = EnumChatFormatting.RED + Translator.translateToLocal("gui.jei.editMode.description.hide.wild");
					list.addAll(minecraft.fontRendererObj.listFormattedStringToWidth(description, Constants.MAX_TOOLTIP_WIDTH));
				}
			}
		}

		return list;
	}
}
