package mezz.jei.common.network.packets;

import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

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
		buf.writeCollection(transferOperations, (b, op) -> op.writePacketData(b));
		buf.writeCollection(craftingSlots, FriendlyByteBuf::writeVarInt);
		buf.writeCollection(inventorySlots, FriendlyByteBuf::writeVarInt);
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
		List<TransferOperation> transferOperations = buf.readList(TransferOperation::readPacketData);
		List<Integer> craftingSlots = buf.readList(FriendlyByteBuf::readVarInt);
		List<Integer> inventorySlots = buf.readList(FriendlyByteBuf::readVarInt);
		boolean maxTransfer = buf.readBoolean();
		boolean requireCompleteSets = buf.readBoolean();

		return new PacketRecipeTransfer(
				transferOperations, craftingSlots, inventorySlots, maxTransfer, requireCompleteSets
		);
	}

}
