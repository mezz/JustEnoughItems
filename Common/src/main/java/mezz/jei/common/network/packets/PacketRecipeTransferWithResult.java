package mezz.jei.common.network.packets;

import mezz.jei.common.Constants;
import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.common.network.packets.legacy.PacketRecipeTransfer;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PacketRecipeTransferWithResult extends PacketJei {
	private final Collection<TransferOperation> transferOperations;
	private final Collection<Slot> craftingSlots;
	private final Collection<Slot> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;
	private final int transferId;

	public static PacketRecipeTransferWithResult fromSlots(
		List<TransferOperation> transferOperations,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets,
		int transferId
	) {
		return new PacketRecipeTransferWithResult(
			transferOperations,
			craftingSlots,
			inventorySlots,
			maxTransfer,
			requireCompleteSets,
			transferId
		);
	}

	public PacketRecipeTransferWithResult(
		Collection<TransferOperation> transferOperations,
		Collection<Slot> craftingSlots,
		Collection<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets,
		int transferId
	) {
		this.transferOperations = transferOperations;
		this.craftingSlots = craftingSlots;
		this.inventorySlots = inventorySlots;
		this.maxTransfer = maxTransfer;
		this.requireCompleteSets = requireCompleteSets;
		this.transferId = transferId;
	}

	@Override
	public ResourceLocation getChannelId() {
		return Constants.RECIPE_TRANSFER_RESULT_CHANNEL_ID;
	}

	@Override
	protected IPacketId getPacketId() {
		return PacketIdServer.RECIPE_TRANSFER_WITH_RESULT;
	}

	@Override
	protected void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(transferOperations.size());
		for (TransferOperation operation : transferOperations) {
			operation.writePacketData(buf);
		}
		PacketRecipeTransfer.writeSlots(buf, craftingSlots);
		PacketRecipeTransfer.writeSlots(buf, inventorySlots);
		buf.writeBoolean(maxTransfer);
		buf.writeBoolean(requireCompleteSets);
		buf.writeVarInt(transferId);
	}

	public static CompletableFuture<Void> readPacketData(ServerPacketData data) {
		ServerPacketContext context = data.context();
		ServerPlayer player = context.player();
		FriendlyByteBuf buf = data.buf();
		AbstractContainerMenu container = player.containerMenu;

		int transferOperationsSize = buf.readVarInt();
		List<TransferOperation> transferOperations = new ArrayList<>();
		for (int i = 0; i < transferOperationsSize; i++) {
			transferOperations.add(TransferOperation.readPacketData(buf, container));
		}

		List<Slot> craftingSlots = PacketRecipeTransfer.readSlots(buf, container, buf.readVarInt());
		List<Slot> inventorySlots = PacketRecipeTransfer.readSlots(buf, container, buf.readVarInt());
		boolean maxTransfer = buf.readBoolean();
		boolean requireCompleteSets = buf.readBoolean();
		int transferId = buf.readVarInt();

		MinecraftServer server = player.server;
		return server.submit(() -> {
			boolean successful = craftingSlots != null && inventorySlots != null &&
				BasicRecipeTransferHandlerServer.setItemsWithResult(
					player,
					transferOperations,
					craftingSlots,
					inventorySlots,
					maxTransfer,
					requireCompleteSets
				);
			sendResult(context, transferId, successful);
		});
	}

	static void sendResult(ServerPacketContext context, int transferId, boolean successful) {
		context.connection().sendPacketToClient(
			new PacketRecipeTransferResult(transferId, successful),
			context.player()
		);
	}
}
