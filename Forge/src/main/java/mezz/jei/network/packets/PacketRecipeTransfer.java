package mezz.jei.network.packets;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.packets.PacketJei;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.transfer.TransferOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PacketRecipeTransfer extends PacketJei {
	public final Collection<TransferOperation> transferOperations;
	public final Collection<Slot> craftingSlots;
	public final Collection<Slot> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;

	public PacketRecipeTransfer(
		Collection<TransferOperation> transferOperations,
		Collection<Slot> craftingSlots,
		Collection<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		this.transferOperations = transferOperations;
		this.craftingSlots = craftingSlots;
		this.inventorySlots = inventorySlots;
		this.maxTransfer = maxTransfer;
		this.requireCompleteSets = requireCompleteSets;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.RECIPE_TRANSFER;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(transferOperations.size());
		for (TransferOperation operation : transferOperations) {
			operation.writePacketData(buf);
		}

		buf.writeVarInt(craftingSlots.size());
		for (Slot craftingSlot : craftingSlots) {
			buf.writeVarInt(craftingSlot.index);
		}

		buf.writeVarInt(inventorySlots.size());
		for (Slot inventorySlot : inventorySlots) {
			buf.writeVarInt(inventorySlot.index);
		}

		buf.writeBoolean(maxTransfer);
		buf.writeBoolean(requireCompleteSets);
	}

	public static void readPacketData(FriendlyByteBuf buf, Player player) {
		AbstractContainerMenu container = player.containerMenu;

		int transferOperationsSize = buf.readVarInt();
		List<TransferOperation> transferOperations = new ArrayList<>(transferOperationsSize);
		for (int i = 0; i < transferOperationsSize; i++) {
			TransferOperation transferOperation = TransferOperation.readPacketData(buf, container);
			transferOperations.add(transferOperation);
		}

		int craftingSlotsSize = buf.readVarInt();
		List<Slot> craftingSlots = new ArrayList<>(craftingSlotsSize);
		for (int i = 0; i < craftingSlotsSize; i++) {
			int slotIndex = buf.readVarInt();
			Slot slot = container.getSlot(slotIndex);
			craftingSlots.add(slot);
		}

		int inventorySlotsSize = buf.readVarInt();
		List<Slot> inventorySlots = new ArrayList<>();
		for (int i = 0; i < inventorySlotsSize; i++) {
			int slotIndex = buf.readVarInt();
			Slot slot = container.getSlot(slotIndex);
			inventorySlots.add(slot);
		}
		boolean maxTransfer = buf.readBoolean();
		boolean requireCompleteSets = buf.readBoolean();

		BasicRecipeTransferHandlerServer.setItems(
			player,
			transferOperations,
			craftingSlots,
			inventorySlots,
			maxTransfer,
			requireCompleteSets
		);
	}

}
