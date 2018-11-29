package mezz.jei.plugins.vanilla.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class EnchantDataRenderer implements IIngredientRenderer<EnchantmentData> {

	private IIngredientRenderer<ItemStack> itemRenderer;
	
	public EnchantDataRenderer() {
		this(new ItemStackRenderer());
	}
	
	public EnchantDataRenderer(IIngredientRenderer<ItemStack> itemRenderer) {
		this.itemRenderer = itemRenderer;
	}
	
	@Override
	public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable EnchantmentData ingredient) {
		if (ingredient != null) {
			ItemStack enchantBook = ItemEnchantedBook.getEnchantedItemStack(ingredient);
			itemRenderer.render(minecraft, xPosition, yPosition, enchantBook);
		}
	}

	@Override
	public List<String> getTooltip(Minecraft minecraft, EnchantmentData ingredient, ITooltipFlag tooltipFlag) {
		EntityPlayer player = minecraft.player;
		ItemStack enchantBook = ItemEnchantedBook.getEnchantedItemStack(ingredient);
		List<String> list;
		try {
			list = enchantBook.getTooltip(player, tooltipFlag);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(enchantBook);
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
}
