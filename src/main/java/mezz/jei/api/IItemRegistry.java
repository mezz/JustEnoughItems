package mezz.jei.api;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IItemRegistry {

	@Nonnull
	public ImmutableList<ItemStack> getItemList();

	@Nonnull
	public ImmutableList<ItemStack> getFuels();

}
