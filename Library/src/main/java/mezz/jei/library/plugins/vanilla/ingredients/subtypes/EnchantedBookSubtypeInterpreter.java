package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class EnchantedBookSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final EnchantedBookSubtypeInterpreter INSTANCE = new EnchantedBookSubtypeInterpreter();

	private EnchantedBookSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(itemStack);
		if (enchantments.isEmpty()) {
			return IIngredientSubtypeInterpreter.NONE;
		}
		List<String> strings = new ArrayList<>();
		for (Holder<Enchantment> e : enchantments.keySet()) {
			Optional<ResourceKey<Enchantment>> enchantmentResourceKey = e.unwrapKey();
			if (enchantmentResourceKey.isPresent()) {
				String s = enchantmentResourceKey.orElseThrow().location() + ".lvl" + enchantments.getLevel(e);
				strings.add(s);
			}
		}

		StringJoiner joiner = new StringJoiner(",", "[", "]");
		strings.sort(null);
		for (String s : strings) {
			joiner.add(s);
		}
		return joiner.toString();
	}
}
