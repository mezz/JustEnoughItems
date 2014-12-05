package mezz.jei.api;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IItemRegistry {

	/** Returns a list of all the Items registered. */
	@Nonnull public ImmutableList<ItemStack> getItemList();

	/** Returns a list of all the Items that can be used as fuel in a vanilla furnace. */
	@Nonnull public ImmutableList<ItemStack> getFuels();

}
