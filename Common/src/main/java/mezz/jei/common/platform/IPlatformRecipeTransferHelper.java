package mezz.jei.common.platform;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Internal platform hooks for recipe transfer sources stored inside item stacks.
 */
public interface IPlatformRecipeTransferHelper {
	boolean hasItemHandler(ItemStack itemStack);

	/**
	 * Returns a snapshot of the item handler's slots, preserving empty slots and their indexes.
	 * An empty optional means the stack has no usable item handler.
	 */
	Optional<List<ItemStack>> getItemHandlerContents(ItemStack itemStack);
}
