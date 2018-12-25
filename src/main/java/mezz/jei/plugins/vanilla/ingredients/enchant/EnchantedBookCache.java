package mezz.jei.plugins.vanilla.ingredients.enchant;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mezz.jei.collect.Table;
import mezz.jei.startup.PlayerJoinedWorldEvent;

public class EnchantedBookCache {
	private final Table<ResourceLocation, Integer, ItemStack> cache = Table.hashBasedTable();

	public EnchantedBookCache() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerJoinedWorldEvent.class, event -> cache.clear());
	}

	public ItemStack getEnchantedBook(EnchantmentData enchantmentData) {
		Enchantment enchantment = enchantmentData.enchantment;
		ResourceLocation registryName = enchantment.getRegistryName();
		if (registryName == null) {
			throw new IllegalArgumentException("Enchantment has no registry name: " + enchantment.getName());
		}
		return cache.computeIfAbsent(registryName, enchantmentData.enchantmentLevel, () -> ItemEnchantedBook.getEnchantedItemStack(enchantmentData));
	}

}
