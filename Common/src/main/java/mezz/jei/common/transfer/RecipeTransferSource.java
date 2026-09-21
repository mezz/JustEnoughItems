package mezz.jei.common.transfer;

import net.minecraft.world.inventory.Slot;

public record RecipeTransferSource(Slot slot, int itemHandlerSlotId) {
	public static final int NO_ITEM_HANDLER_SLOT = -1;

	public RecipeTransferSource {
		if (itemHandlerSlotId < NO_ITEM_HANDLER_SLOT) {
			throw new IllegalArgumentException("Item handler slot id must be -1 or greater");
		}
	}

	public RecipeTransferSource(Slot slot) {
		this(slot, NO_ITEM_HANDLER_SLOT);
	}

	public boolean isItemHandlerSource() {
		return itemHandlerSlotId != NO_ITEM_HANDLER_SLOT;
	}

	public TransferOperation createTransferOperation(Slot craftingSlot, int count) {
		return new TransferOperation(slot.index, craftingSlot.index, count, itemHandlerSlotId);
	}
}
