package mezz.jei.common.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

public interface IPlatformItemStackHelper {
	int getBurnTime(ItemStack itemStack);

	boolean isBookEnchantable(ItemStack stack, ItemStack book);

	Optional<String> getCreatorModId(ItemStack stack);

	default ItemAttributeModifiers getItemAttributeModifiers(ItemStack stack) {
		return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
	}

	boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient);
}
