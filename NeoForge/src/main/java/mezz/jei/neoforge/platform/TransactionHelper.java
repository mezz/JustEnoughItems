package mezz.jei.neoforge.platform;

import mezz.jei.common.platform.IPlatformTransactionHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.IdentityHashMap;
import java.util.Map;

public class TransactionHelper implements IPlatformTransactionHelper {
	@Override
	public ITransaction openTransaction() {
		return new NeoForgeTransaction(Transaction.openRoot());
	}

	private static class NeoForgeTransaction implements ITransaction {
		private final Transaction transaction;
		private final Map<Slot, SlotJournal> slotJournals;
		private final Map<ItemStack, ResourceHandler<ItemResource>> itemHandlers;

		public NeoForgeTransaction(Transaction transaction) {
			this(transaction, new IdentityHashMap<>(), new IdentityHashMap<>());
		}

		private NeoForgeTransaction(
			Transaction transaction,
			Map<Slot, SlotJournal> slotJournals,
			Map<ItemStack, ResourceHandler<ItemResource>> itemHandlers
		) {
			this.transaction = transaction;
			this.slotJournals = slotJournals;
			this.itemHandlers = itemHandlers;
		}

		@Override
		public ITransaction openNested() {
			return new NeoForgeTransaction(Transaction.open(transaction), slotJournals, itemHandlers);
		}

		@Override
		public ItemStack extractFromSlot(Slot slot, int amount, Player player) {
			return getSlotJournal(slot).extract(amount, player, transaction);
		}

		@Override
		public ItemStack extractFromItemHandler(ItemStack itemStack, int itemHandlerSlot, ItemStack expectedStack) {
			ResourceHandler<ItemResource> itemHandler = itemHandlers.computeIfAbsent(itemStack, RecipeTransferHelper::getItemHandler);
			if (itemHandler == null || itemHandlerSlot < 0 || itemHandlerSlot >= itemHandler.size()) {
				return ItemStack.EMPTY;
			}

			ItemResource resource = itemHandler.getResource(itemHandlerSlot);
			int expectedCount = expectedStack.getCount();
			if (!resource.matches(expectedStack) || itemHandler.getAmountAsInt(itemHandlerSlot) < expectedCount) {
				return ItemStack.EMPTY;
			}

			int extracted = itemHandler.extract(itemHandlerSlot, resource, expectedCount, transaction);
			return resource.toStack(extracted);
		}

		@Override
		public ItemStack insertIntoSlot(Slot slot, ItemStack stack, int amount) {
			return getSlotJournal(slot).insert(stack, amount, transaction);
		}

		private SlotJournal getSlotJournal(Slot slot) {
			return slotJournals.computeIfAbsent(slot, SlotJournal::new);
		}

		@Override
		public void commit() {
			transaction.commit();
		}

		@Override
		public void close() {
			transaction.close();
		}
	}

	private static class SlotJournal extends SnapshotJournal<ItemStack> {
		private final Slot slot;

		public SlotJournal(Slot slot) {
			this.slot = slot;
		}

		public ItemStack extract(int amount, Player player, TransactionContext transaction) {
			if (amount <= 0 || !slot.mayPickup(player)) {
				return ItemStack.EMPTY;
			}

			ItemStack currentStack = slot.getItem();
			if (currentStack.isEmpty() || !canExtractAmount(player, amount, currentStack)) {
				return ItemStack.EMPTY;
			}

			int extractedAmount = Math.min(amount, currentStack.getCount());
			updateSnapshots(transaction);
			ItemStack extractedStack = slot.remove(extractedAmount);
			if (extractedStack.isEmpty()) {
				return ItemStack.EMPTY;
			}
			if (slot.getItem().isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY, extractedStack);
			}
			new RootCommitJournal(() -> slot.onTake(player, extractedStack))
				.updateSnapshots(transaction);
			return extractedStack;
		}

		private boolean canExtractAmount(Player player, int amount, ItemStack currentStack) {
			return slot.allowModification(player) || amount >= currentStack.getCount();
		}

		public ItemStack insert(ItemStack stack, int amount, TransactionContext transaction) {
			if (stack.isEmpty() || amount <= 0 || !slot.mayPlace(stack)) {
				return stack;
			}

			ItemStack currentStack = slot.getItem();
			int insertedAmount = getMaxInsertAmount(slot, stack, amount);
			if (insertedAmount <= 0) {
				return stack;
			}

			if (currentStack.isEmpty()) {
				updateSnapshots(transaction);
				slot.setByPlayer(stack.copyWithCount(insertedAmount), currentStack);
				return getRemainder(stack, insertedAmount);
			}

			if (ItemStack.isSameItemSameComponents(currentStack, stack)) {
				ItemStack insertedStack = currentStack.copy();
				insertedStack.grow(insertedAmount);
				updateSnapshots(transaction);
				slot.setByPlayer(insertedStack, currentStack);
				return getRemainder(stack, insertedAmount);
			}

			return stack;
		}

		private static int getMaxInsertAmount(Slot slot, ItemStack stack, int amount) {
			int remainingSlotSpace = slot.getMaxStackSize(stack) - slot.getItem().getCount();
			return Math.min(
				Math.min(amount, stack.getCount()),
				Math.max(0, remainingSlotSpace)
			);
		}

		private static ItemStack getRemainder(ItemStack stack, int insertedAmount) {
			int remaining = stack.getCount() - insertedAmount;
			if (remaining == 0) {
				return ItemStack.EMPTY;
			}
			return stack.copyWithCount(remaining);
		}

		@Override
		protected ItemStack createSnapshot() {
			return slot.getItem().copy();
		}

		@Override
		protected void revertToSnapshot(ItemStack snapshot) {
			slot.set(snapshot.copy());
		}

		@Override
		protected void onRootCommit(ItemStack originalState) {
			slot.setChanged();
		}
	}
}
