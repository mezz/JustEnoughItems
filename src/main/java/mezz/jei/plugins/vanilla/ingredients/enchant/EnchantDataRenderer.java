package mezz.jei.plugins.vanilla.ingredients.enchant;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredientRenderer;

public class EnchantDataRenderer implements IIngredientRenderer<EnchantmentData> {

	private IIngredientRenderer<ItemStack> itemRenderer;
	private EnchantedBookCache cache;

	public EnchantDataRenderer(IIngredientRenderer<ItemStack> itemRenderer, EnchantedBookCache cache) {
		this.itemRenderer = itemRenderer;
		this.cache = cache;
	}

	@Override
	public void render(int xPosition, int yPosition, @Nullable EnchantmentData ingredient) {
		if (ingredient != null) {
			ItemStack enchantBook = cache.getEnchantedBook(ingredient);
			itemRenderer.render(xPosition, yPosition, enchantBook);
		}
	}

	@Override
	public List<String> getTooltip(EnchantmentData ingredient, ITooltipFlag tooltipFlag) {
		ItemStack enchantBook = cache.getEnchantedBook(ingredient);
		return itemRenderer.getTooltip(enchantBook, tooltipFlag);
	}

	@Override
	public FontRenderer getFontRenderer(Minecraft minecraft, EnchantmentData ingredient) {
		ItemStack enchantBook = cache.getEnchantedBook(ingredient);
		return itemRenderer.getFontRenderer(minecraft, enchantBook);
	}
}
