package mezz.jei.api;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface IItemRegistry {

	@Nonnull
	public List<ItemStack> getItemList();

	@Nonnull
	public List<ItemStack> getFuels();

}
