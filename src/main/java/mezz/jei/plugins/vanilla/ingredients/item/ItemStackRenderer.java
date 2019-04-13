package mezz.jei.plugins.vanilla.ingredients.item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraftforge.common.IRarity;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	@Override
	public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
		if (ingredient != null) {
			GlStateManager.enableDepth();
			RenderHelper.enableGUIStandardItemLighting();
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, ingredient, xPosition, yPosition);
			minecraft.getRenderItem().renderItemOverlayIntoGUI(font, ingredient, xPosition, yPosition, null);
			GlStateManager.disableBlend();
			RenderHelper.disableStandardItemLighting();
		}
	}

	@Override
	public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, ITooltipFlag tooltipFlag) {
		EntityPlayer player = minecraft.player;
		List<String> list;
		try {
			list = ingredient.getTooltip(player, tooltipFlag);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			Log.get().error("Failed to get tooltip: {}", itemStackInfo, e);
			list = new ArrayList<>();
			list.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
			return list;
		}

		IRarity rarity;
		try {
			rarity = ingredient.getItem().getForgeRarity(ingredient);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			Log.get().error("Failed to get rarity: {}", itemStackInfo, e);
			rarity = EnumRarity.COMMON;
		}

		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, rarity.getColor() + list.get(k));
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
			fontRenderer = minecraft.fontRenderer;
		}
		return fontRenderer;
	}
}
