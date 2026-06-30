package mezz.jei.common.transfer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Represents transferring an ItemStack from inventorySlot to craftingSlot.
 */
public record TransferOperation(int inventorySlotId, int craftingSlotId, int count) {
	public static TransferOperation readPacketData(FriendlyByteBuf buf, AbstractContainerMenu container) {
		int inventorySlotId = buf.readVarInt();
		int craftingSlotId = buf.readVarInt();
		return new TransferOperation(inventorySlotId, craftingSlotId);
	}

	public static TransferOperation readCountedPacketData(FriendlyByteBuf buf, AbstractContainerMenu container) {
		int inventorySlotId = buf.readVarInt();
		int craftingSlotId = buf.readVarInt();
		int count = buf.readVarInt();
		return new TransferOperation(inventorySlotId, craftingSlotId, count);
	}

	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(inventorySlotId);
		buf.writeVarInt(craftingSlotId);
	}

	public void writeCountedPacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(inventorySlotId);
		buf.writeVarInt(craftingSlotId);
		buf.writeVarInt(count);
	}

	public TransferOperation(int inventorySlotId, int craftingSlotId) {
		this(inventorySlotId, craftingSlotId, 1);
	}

	public TransferOperation {
		if (count < 1) {
			throw new IllegalArgumentException("Transfer operation count must be positive");
		}
	}

	public Slot inventorySlot(AbstractContainerMenu container) {
		return container.getSlot(inventorySlotId);
	}

	public Slot craftingSlot(AbstractContainerMenu container) {
		return container.getSlot(craftingSlotId);
	}
}
