package mezz.jei.render;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.input.ClickedIngredient;
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

public class GuiIngredientFastList {
	private final List<GuiIngredientFast> renderAll = new ArrayList<>();

	private final List<GuiIngredientFast> renderItems2d = new ArrayList<>();
	private final List<GuiIngredientFast> renderItems3d = new ArrayList<>();
	private final List<GuiIngredientFast> renderOther = new ArrayList<>();

	private final IIngredientRegistry ingredientRegistry;

	private int blocked = 0;

	public GuiIngredientFastList(IIngredientRegistry ingredientRegistry) {
		this.ingredientRegistry = ingredientRegistry;
	}

	public void clear() {
		renderAll.clear();

		renderItems2d.clear();
		renderItems3d.clear();
		renderOther.clear();
		blocked = 0;
	}

	public int size() {
		return renderAll.size() - blocked;
	}

	public void add(GuiIngredientFast guiItemStack) {
		renderAll.add(guiItemStack);
	}

	public List<GuiIngredientFast> getAllGuiIngredients() {
		return renderAll;
	}

	public void set(final int startIndex, List<IIngredientListElement> ingredientList) {
		renderItems2d.clear();
		renderItems3d.clear();
		renderOther.clear();
		blocked = 0;

		int i = startIndex;
		for (GuiIngredientFast guiItemStack : renderAll) {
			if (guiItemStack.isBlocked()) {
				guiItemStack.clear();
				blocked++;
			} else {
				if (i >= ingredientList.size()) {
					guiItemStack.clear();
				} else {
					IIngredientListElement<?> element = ingredientList.get(i);
					set(guiItemStack, element);
				}
				i++;
			}
		}
	}

	private <V> void set(GuiIngredientFast guiItemStack, IIngredientListElement<V> element) {
		guiItemStack.setElement(element);

		V ingredient = element.getIngredient();
		if (ingredient instanceof ItemStack) {
			ItemStack stack = (ItemStack) ingredient;
			RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
			IBakedModel bakedModel;
			try {
				bakedModel = renderItem.getItemModelWithOverrides(stack, null, null);
			} catch (Throwable throwable) {
				IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
				String stackInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.get().error("ItemStack crashed getting IBakedModel. {}", stackInfo, throwable);
				guiItemStack.clear();
				return;
			}
			//noinspection ConstantConditions
			if (bakedModel == null) {
				IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);
				String stackInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.get().error("ItemStack returned null IBakedModel. {}", stackInfo, new NullPointerException());
				guiItemStack.clear();
			} else {
				if (bakedModel.isBuiltInRenderer()) {
					renderOther.add(guiItemStack);
				} else if (bakedModel.isGui3d()) {
					renderItems3d.add(guiItemStack);
				} else {
					renderItems2d.add(guiItemStack);
				}
			}
		} else {
			renderOther.add(guiItemStack);
		}
	}

	@Nullable
	public ClickedIngredient<?> getIngredientUnderMouse(int mouseX, int mouseY) {
		GuiIngredientFast hovered = getHovered(mouseX, mouseY);
		if (hovered != null) {
			IIngredientListElement element = hovered.getElement();
			if (element != null) {
				return new ClickedIngredient<>(element.getIngredient());
			}
		}
		return null;
	}

	@Nullable
	public GuiIngredientFast getHovered(int mouseX, int mouseY) {
		for (GuiIngredientFast guiItemStack : renderAll) {
			if (guiItemStack.isMouseOver(mouseX, mouseY)) {
				return guiItemStack;
			}
		}
		return null;
	}

	/**
	 * renders all ItemStacks
	 */
	public void render(Minecraft minecraft) {
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
		for (GuiIngredientFast guiItemStack : renderItems3d) {
			guiItemStack.renderItemAndEffectIntoGUI();
		}

		// 2d Items
		GlStateManager.disableLighting();
		for (GuiIngredientFast guiItemStack : renderItems2d) {
			guiItemStack.renderItemAndEffectIntoGUI();
		}

		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();

		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

		renderItem.zLevel -= 50.0F;

		// overlays
		for (GuiIngredientFast guiItemStack : renderItems3d) {
			guiItemStack.renderOverlay(minecraft);
		}

		for (GuiIngredientFast guiItemStack : renderItems2d) {
			guiItemStack.renderOverlay(minecraft);
		}

		GlStateManager.disableLighting();

		// other rendering
		for (GuiIngredientFast guiItemStack : renderOther) {
			guiItemStack.renderSlow();
		}

		RenderHelper.disableStandardItemLighting();
	}
}
