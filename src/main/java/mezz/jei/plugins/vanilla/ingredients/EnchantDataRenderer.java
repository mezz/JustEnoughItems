package mezz.jei.plugins.vanilla.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class EnchantDataRenderer implements IIngredientRenderer<EnchantmentData> {

	@Override
	public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable EnchantmentData ingredient) {
		if (ingredient != null) {
			GlStateManager.enableDepth();
			RenderHelper.enableGUIStandardItemLighting();
			FontRenderer font = getFontRenderer(minecraft, ingredient);
			ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
			ItemEnchantedBook.addEnchantment(enchantedBook, ingredient);
			minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, enchantedBook, xPosition, yPosition);
			minecraft.getRenderItem().renderItemOverlayIntoGUI(font, enchantedBook, xPosition, yPosition, null);
			GlStateManager.disableBlend();
			RenderHelper.disableStandardItemLighting();
		}
	}

	@Override
	public List<String> getTooltip(Minecraft minecraft, EnchantmentData ingredient, ITooltipFlag tooltipFlag) {
		EntityPlayer player = minecraft.player;
		ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
		ItemEnchantedBook.addEnchantment(enchantedBook, ingredient);
		List<String> list;
		try {
			list = enchantedBook.getTooltip(player, tooltipFlag);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(enchantedBook);
			Log.get().error("Failed to get tooltip: {}", itemStackInfo, e);
			list = new ArrayList<>();
			list.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.error.crash"));
			return list;
		}

		EnumRarity rarity;
		if (ingredient.enchantment.isTreasureEnchantment()) {
			rarity = EnumRarity.RARE;
		} else {
			rarity = EnumRarity.UNCOMMON;
		}

		for (int k = 0; k < list.size(); ++k) {
			if (k == 0) {
				list.set(k, rarity.color + list.get(k));
			} else {
				list.set(k, TextFormatting.GRAY + list.get(k));
			}
		}

		return list;
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, EnchantmentData ingredient) {
		FontRenderer fontRenderer = Items.ENCHANTED_BOOK.getFontRenderer(new ItemStack(Items.ENCHANTED_BOOK));
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRenderer;
		}
		return minecraft.fontRenderer;
	}
}