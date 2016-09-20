package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.gui.Focus;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.ItemStackElement;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class GuiItemStackFastList {
	private final List<GuiItemStackFast> renderItemsAll = new ArrayList<GuiItemStackFast>();
	private final List<GuiItemStackFast> renderItemsBuiltIn = new ArrayList<GuiItemStackFast>();
	private final List<GuiItemStackFast> renderItems2d = new ArrayList<GuiItemStackFast>();
	private final List<GuiItemStackFast> renderItems3d = new ArrayList<GuiItemStackFast>();

	public void clear() {
		renderItemsAll.clear();
		renderItemsBuiltIn.clear();
		renderItems2d.clear();
		renderItems3d.clear();
	}

	public int size() {
		return renderItemsAll.size();
	}

	public void add(GuiItemStackFast guiItemStack) {
		renderItemsAll.add(guiItemStack);
	}

	public List<GuiItemStackFast> getAllGuiStacks() {
		return renderItemsAll;
	}

	public void set(int i, List<ItemStackElement> itemList) {
		renderItemsBuiltIn.clear();
		renderItems2d.clear();
		renderItems3d.clear();

		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

		for (GuiItemStackFast guiItemStack : renderItemsAll) {
			if (i >= itemList.size()) {
				guiItemStack.clear();
			} else {
				ItemStack stack = itemList.get(i).getItemStack();
				IBakedModel bakedModel = renderItem.getItemModelWithOverrides(stack, null, null);
				if (bakedModel == null) {
					String stackInfo = ErrorUtil.getItemStackInfo(stack);
					Log.error("ItemStack returned null IBakedModel from RenderItem.getItemModelWithOverrides(stack, null, null). " + stackInfo, new NullPointerException());
				} else {
					guiItemStack.setItemStack(stack);
					if (bakedModel.isBuiltInRenderer()) {
						renderItemsBuiltIn.add(guiItemStack);
					} else if (bakedModel.isGui3d()) {
						renderItems3d.add(guiItemStack);
					} else {
						renderItems2d.add(guiItemStack);
					}
				}
			}
			i++;
		}
	}

	@Nullable
	public Focus<?> getFocusUnderMouse(int mouseX, int mouseY) {
		GuiItemStackFast hovered = getHovered(mouseX, mouseY);
		if (hovered != null) {
			return new Focus<ItemStack>(hovered.getItemStack());
		}
		return null;
	}

	@Nullable
	private GuiItemStackFast getHovered(int mouseX, int mouseY) {
		for (GuiItemStackFast guiItemStack : renderItemsAll) {
			if (guiItemStack.isMouseOver(mouseX, mouseY)) {
				return guiItemStack;
			}
		}
		return null;
	}

	/** renders all ItemStacks and returns hovered gui item stack for later render pass */
	@Nullable
	public GuiItemStackFast render(Minecraft minecraft, boolean isMouseOver, int mouseX, int mouseY) {
		GuiItemStackFast hovered = null;
		if (isMouseOver) {
			hovered = getHovered(mouseX, mouseY);
		}

		RenderHelper.enableGUIStandardItemLighting();

		RenderItem renderItem = minecraft.getRenderItem();
		TextureManager textureManager = minecraft.getTextureManager();
		renderItem.zLevel += 50.0F;

		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// 3d Items
		GlStateManager.enableLighting();
		for (GuiItemStackFast guiItemStack : renderItems3d) {
			if (hovered != guiItemStack) {
				guiItemStack.renderItemAndEffectIntoGUI();
			}
		}

		// 2d Items
		GlStateManager.disableLighting();
		for (GuiItemStackFast guiItemStack : renderItems2d) {
			if (hovered != guiItemStack) {
				guiItemStack.renderItemAndEffectIntoGUI();
			}
		}

		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();

		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

		renderItem.zLevel -= 50.0F;

		// built-in render Items
		for (GuiItemStackFast guiItemStack : renderItemsBuiltIn) {
			if (hovered != guiItemStack) {
				guiItemStack.renderSlow();
			}
		}

		for (GuiItemStackFast guiItemStack : renderItemsAll) {
			if (hovered != guiItemStack) {
				guiItemStack.renderOverlay(minecraft);
			}
		}

		RenderHelper.disableStandardItemLighting();

		return hovered;
	}
}
