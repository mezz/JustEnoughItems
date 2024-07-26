package mezz.jei.library.plugins.vanilla.ingredients.subtypes;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class EnchantedBookSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final EnchantedBookSubtypeInterpreter INSTANCE = new EnchantedBookSubtypeInterpreter();

	private EnchantedBookSubtypeInterpreter() {

	}

	@Override
	public String apply(ItemStack itemStack, UidContext context) {
		List<String> strings = new ArrayList<>();
		ListTag enchantments = EnchantedBookItem.getEnchantments(itemStack);
		for (int i = 0; i < enchantments.size(); ++i) {
			CompoundTag compoundnbt = enchantments.getCompound(i);
			String id = compoundnbt.getString("id");
			IPlatformRegistry<Enchantment> enchantmentRegistry = Services.PLATFORM.getRegistry(Registries.ENCHANTMENT);
			ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
			if (resourceLocation != null) {
				enchantmentRegistry.getValue(resourceLocation)
					.map(enchantment -> enchantment.getDescriptionId() + ".lvl" + compoundnbt.getShort("lvl"))
					.ifPresent(strings::add);
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
