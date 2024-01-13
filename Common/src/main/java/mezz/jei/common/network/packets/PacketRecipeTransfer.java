package mezz.jei.common.network.packets;

import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class PacketRecipeTransfer extends PacketJeiToServer {
	public final List<TransferOperation> transferOperations;
	public final List<Integer> craftingSlots;
	public final List<Integer> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;

	public static PacketRecipeTransfer fromSlots(
			List<TransferOperation> transferOperations,
			List<Slot> craftingSlots,
			List<Slot> inventorySlots,
			boolean maxTransfer,
			boolean requireCompleteSets
	) {
		return new PacketRecipeTransfer(
				transferOperations,
				craftingSlots.stream().map(s -> s.index).toList(),
				inventorySlots.stream().map(s -> s.index).toList(),
				maxTransfer,
				requireCompleteSets
		);
	}

	public PacketRecipeTransfer(
		List<TransferOperation> transferOperations,
		List<Integer> craftingSlots,
		List<Integer> inventorySlots,
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
	public PacketIdServer getPacketId() {
		return PacketIdServer.RECIPE_TRANSFER;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(transferOperations.size());
		for (TransferOperation operation : transferOperations) {
			operation.writePacketData(buf);
		}

		buf.writeVarInt(craftingSlots.size());
		for (int craftingSlot : craftingSlots) {
			buf.writeVarInt(craftingSlot);
		}

		buf.writeVarInt(inventorySlots.size());
		for (int inventorySlot : inventorySlots) {
			buf.writeVarInt(inventorySlot);
		}

		buf.writeBoolean(maxTransfer);
		buf.writeBoolean(requireCompleteSets);
	}

	@Override
	public void processOnServerThread(ServerPacketContext context) {
		AbstractContainerMenu container = context.player().containerMenu;
		BasicRecipeTransferHandlerServer.setItems(
				context.player(),
				transferOperations,
				craftingSlots.stream().map(container::getSlot).toList(),
				inventorySlots.stream().map(container::getSlot).toList(),
				maxTransfer,
				requireCompleteSets
		);
	}

	public static PacketRecipeTransfer readPacketData(FriendlyByteBuf buf) {
		int transferOperationsSize = buf.readVarInt();
		List<TransferOperation> transferOperations = new ArrayList<>();
		for (int i = 0; i < transferOperationsSize; i++) {
			TransferOperation transferOperation = TransferOperation.readPacketData(buf);
			transferOperations.add(transferOperation);
		}

		int craftingSlotsSize = buf.readVarInt();
		List<Integer> craftingSlots = new ArrayList<>();
		for (int i = 0; i < craftingSlotsSize; i++) {
			int slotIndex = buf.readVarInt();
			craftingSlots.add(slotIndex);
		}

		int inventorySlotsSize = buf.readVarInt();
		List<Integer> inventorySlots = new ArrayList<>();
		for (int i = 0; i < inventorySlotsSize; i++) {
			int slotIndex = buf.readVarInt();
			inventorySlots.add(slotIndex);
		}
		boolean maxTransfer = buf.readBoolean();
		boolean requireCompleteSets = buf.readBoolean();

		return new PacketRecipeTransfer(
				transferOperations, craftingSlots, inventorySlots, maxTransfer, requireCompleteSets
		);
	}

}
