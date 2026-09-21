package mezz.jei.common.platform;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Internal platform hooks for transactional recipe transfer operations.
 */
public interface IPlatformTransactionHelper {
	ITransaction openTransaction();

	interface ITransaction extends AutoCloseable {
		ITransaction openNested();

		ItemStack extractFromSlot(Slot slot, int amount, Player player);

		ItemStack extractFromItemHandler(ItemStack itemStack, int itemHandlerSlot, ItemStack expectedStack);

		ItemStack insertIntoSlot(Slot slot, ItemStack stack, int amount);

		void commit();

		@Override
		void close();
	}
}
