package mezz.jei.common.transfer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Represents transferring an ItemStack from inventorySlot to craftingSlot.
 */
public record TransferOperation(int inventorySlotId, int craftingSlotId) {
	public static TransferOperation readPacketData(FriendlyByteBuf buf) {
		int inventorySlotIndex = buf.readVarInt();
		int craftingSlotIndex = buf.readVarInt();
		return new TransferOperation(inventorySlotIndex, craftingSlotIndex);
	}

	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(inventorySlotId);
		buf.writeVarInt(craftingSlotId);
	}

	public Slot inventorySlot(AbstractContainerMenu container) {
		return container.getSlot(inventorySlotId);
	}

	public Slot craftingSlot(AbstractContainerMenu container) {
		return container.getSlot(craftingSlotId);
	}
}
