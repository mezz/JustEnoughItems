package mezz.jei.plugins.vanilla.ingredients.enchant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;

public final class EnchantDataListFactory {
	private EnchantDataListFactory() {

	}

	public static List<EnchantmentData> create() {
		List<EnchantmentData> enchantData = new ArrayList<>();

		Collection<Enchantment> enchants = ForgeRegistries.ENCHANTMENTS.getValuesCollection();
		for (Enchantment enchant : enchants) {
			for (int lvl = enchant.getMinLevel(); lvl <= enchant.getMaxLevel(); lvl++) {
				enchantData.add(new EnchantmentData(enchant, lvl));
			}
		}

		return enchantData;
	}
}
