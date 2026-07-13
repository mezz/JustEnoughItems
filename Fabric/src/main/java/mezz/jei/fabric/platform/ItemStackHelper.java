package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
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
		return Optional.empty();
	}

	@Override
	public Collection<CreativeModeTab> getCreativeTabs(ItemStack itemStack) {
		CreativeModeTab creativeTab = itemStack.getItem().getItemCategory();
		if (creativeTab == null) {
			return List.of();
		}
		return List.of(creativeTab);
	}
}
