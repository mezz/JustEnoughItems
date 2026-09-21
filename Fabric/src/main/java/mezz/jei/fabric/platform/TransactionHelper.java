package mezz.jei.fabric.platform;

import mezz.jei.common.platform.IPlatformTransactionHelper;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TransactionHelper implements IPlatformTransactionHelper {
	@Override
	public ITransaction openTransaction() {
		return new FabricTransaction(Transaction.openOuter());
	}

	private static class FabricTransaction implements ITransaction {
		private final Transaction transaction;
		private final Map<Slot, SlotParticipant> slotParticipants;

		public FabricTransaction(Transaction transaction) {
			this(transaction, new IdentityHashMap<>());
		}

		private FabricTransaction(Transaction transaction, Map<Slot, SlotParticipant> slotParticipants) {
			this.transaction = transaction;
			this.slotParticipants = slotParticipants;
		}

		@Override
		public ITransaction openNested() {
			return new FabricTransaction(transaction.openNested(), slotParticipants);
		}

		@Override
		public ItemStack extractFromSlot(Slot slot, int amount, Player player) {
			return getSlotParticipant(slot).extract(amount, player, transaction, this::runOnRootCommit);
		}

		@Override
		public ItemStack extractFromItemHandler(ItemStack itemStack, int itemHandlerSlot, ItemStack expectedStack) {
			return ItemStack.EMPTY;
		}

		private void runOnRootCommit(Runnable runnable) {
			transaction.addCloseCallback((closedTransaction, result) -> {
				if (result.wasCommitted()) {
					closedTransaction.addOuterCloseCallback(outerResult -> {
						if (outerResult.wasCommitted()) {
							runnable.run();
						}
					});
				}
			});
		}

		@Override
		public ItemStack insertIntoSlot(Slot slot, ItemStack stack, int amount) {
			return getSlotParticipant(slot).insert(stack, amount, transaction);
		}

		private SlotParticipant getSlotParticipant(Slot slot) {
			return slotParticipants.computeIfAbsent(slot, SlotParticipant::new);
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

	private static class SlotParticipant extends SnapshotParticipant<ItemStack> {
		private final Slot slot;

		public SlotParticipant(Slot slot) {
			this.slot = slot;
		}

		public ItemStack extract(
			int amount,
			Player player,
			TransactionContext transaction,
			Consumer<Runnable> runOnRootCommit
		) {
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
			runOnRootCommit.accept(() -> slot.onTake(player, extractedStack));
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
		protected void readSnapshot(ItemStack snapshot) {
			slot.set(snapshot.copy());
		}

		@Override
		protected void onFinalCommit() {
			slot.setChanged();
		}
	}
}
