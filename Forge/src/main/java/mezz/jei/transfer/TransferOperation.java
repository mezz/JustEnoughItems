package mezz.jei.transfer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Represents transferring an ItemStack from inventorySlot to craftingSlot.
 */
public record TransferOperation(Slot inventorySlot, Slot craftingSlot) {
	public static TransferOperation readPacketData(FriendlyByteBuf buf, AbstractContainerMenu container) {
		int inventorySlotIndex = buf.readVarInt();
		int craftingSlotIndex = buf.readVarInt();

		Slot inventorySlot = container.getSlot(inventorySlotIndex);
		Slot craftingSlot = container.getSlot(craftingSlotIndex);
		return new TransferOperation(inventorySlot, craftingSlot);
	}

	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(inventorySlot.index);
		buf.writeVarInt(craftingSlot.index);
	}
}
