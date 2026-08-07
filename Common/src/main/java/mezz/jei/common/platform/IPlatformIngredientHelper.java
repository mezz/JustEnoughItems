package mezz.jei.common.platform;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IPlatformIngredientHelper {
	float getCompostValue(ItemStack itemStack);

	HolderSet<Item> getSupportedItems(Holder<Enchantment> enchantment);
}
