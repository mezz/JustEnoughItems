package mezz.jei.common.platform;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Optional;

public interface IPlatformItemStackHelper {
	int getBurnTime(ItemStack itemStack);

	boolean isBookEnchantable(ItemStack stack, ItemStack book);

	Optional<String> getCreatorModId(ItemStack stack);

	Collection<CreativeModeTab> getCreativeTabs(ItemStack itemStack);
}
