package mezz.jei.plugins.vanilla.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.FakeClientPlayer;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	@Override
	public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
		if (ingredient != null) {
			RenderHelper.enableGUIStandardItemLighting();
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, ingredient, xPosition, yPosition);
			minecraft.getRenderItem().renderItemOverlayIntoGUI(font, ingredient, xPosition, yPosition, null);
			GlStateManager.disableBlend();
			RenderHelper.disableStandardItemLighting();
		}
	}

	@Override
	public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient) {
		EntityPlayer player = minecraft.player;
		if (player == null) {
			player = FakeClientPlayer.getInstance();
		}

		List<String> list;
		try {
			list = ingredient.getTooltip(player, minecraft.gameSettings.advancedItemTooltips);
		} catch (RuntimeException e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			Log.error("Failed to get tooltip: {}", itemStackInfo, e);
			list = new ArrayList<String>();
			list.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
			return list;
		} catch (LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			Log.error("Failed to get tooltip: {}", itemStackInfo, e);
			list = new ArrayList<String>();
			list.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
			return list;
		}

		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, ingredient.getRarity().rarityColor + list.get(k));
			} else {
				list.set(k, TextFormatting.GRAY + list.get(k));
			}
		}

		return list;
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		FontRenderer fontRenderer = ingredient.getItem().getFontRenderer(ingredient);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}
}
