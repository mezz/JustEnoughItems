package mezz.jei.common.network.packets.legacy;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.common.network.packets.PacketJei;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PacketRecipeTransferCounted extends PacketJei {
	public final Collection<TransferOperation> transferOperations;
	public final Collection<Slot> craftingSlots;
	public final Collection<Slot> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;

	public PacketRecipeTransferCounted(
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
		return PacketIdServer.RECIPE_TRANSFER_COUNTED;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(transferOperations.size());
		for (TransferOperation operation : transferOperations) {
			operation.writeCountedPacketData(buf);
		}

		PacketRecipeTransfer.writeSlots(buf, craftingSlots);
		PacketRecipeTransfer.writeSlots(buf, inventorySlots);
		buf.writeBoolean(maxTransfer);
		buf.writeBoolean(requireCompleteSets);
	}

	public static CompletableFuture<Void> readPacketData(ServerPacketData data) {
		ServerPacketContext context = data.context();
		ServerPlayer player = context.player();
		FriendlyByteBuf buf = data.buf();
		AbstractContainerMenu container = player.containerMenu;

		int transferOperationsSize = buf.readVarInt();
		List<TransferOperation> transferOperations = new ArrayList<>();
		for (int i = 0; i < transferOperationsSize; i++) {
			transferOperations.add(TransferOperation.readCountedPacketData(buf, container));
		}

		List<Slot> craftingSlots = PacketRecipeTransfer.readSlots(buf, container, buf.readVarInt());
		if (craftingSlots == null) {
			return CompletableFuture.completedFuture(null);
		}
		List<Slot> inventorySlots = PacketRecipeTransfer.readSlots(buf, container, buf.readVarInt());
		if (inventorySlots == null) {
			return CompletableFuture.completedFuture(null);
		}
		boolean maxTransfer = buf.readBoolean();
		boolean requireCompleteSets = buf.readBoolean();

		MinecraftServer server = player.server;
		return server.submit(() ->
			BasicRecipeTransferHandlerServer.setItems(
				player,
				transferOperations,
				craftingSlots,
				inventorySlots,
				maxTransfer,
				requireCompleteSets
			)
		);
	}
}
