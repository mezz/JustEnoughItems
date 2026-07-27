package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Objects;
import java.util.Optional;

public class ItemStackHelper implements IPlatformItemStackHelper {
	@Override
	public int getBurnTime(ItemStack itemStack) {
		return Objects.requireNonNullElse(FuelRegistry.INSTANCE.get(itemStack.getItem()), 0);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return true;
	}

	@Override
	public Optional<String> getCreatorModId(ItemStack stack) {
		return Optional.of(stack.getCreatorNamespace());
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canEnchant(Holder<Enchantment> enchantment, ItemStack ingredient) {
		return enchantment.value().canEnchant(ingredient);
	}
}
