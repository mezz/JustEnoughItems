package mezz.jei.common.network.packets;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PacketRecipeTransfer extends PacketJei {
	private static final Logger LOGGER = LogManager.getLogger();

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

	public static CompletableFuture<Void> readPacketData(ServerPacketData data) {
		ServerPacketContext context = data.context();
		ServerPlayer player = context.player();
		FriendlyByteBuf buf = data.buf();
		AbstractContainerMenu container = player.containerMenu;

		int transferOperationsSize = buf.readVarInt();
		List<TransferOperation> transferOperations = new ArrayList<>();
		for (int i = 0; i < transferOperationsSize; i++) {
			TransferOperation transferOperation = TransferOperation.readPacketData(buf, container);
			transferOperations.add(transferOperation);
		}

		int craftingSlotsSize = buf.readVarInt();
		List<Slot> craftingSlots = readSlots(buf, container, craftingSlotsSize);
		if (craftingSlots == null) {
			return CompletableFuture.completedFuture(null);
		}

		int inventorySlotsSize = buf.readVarInt();
		List<Slot> inventorySlots = readSlots(buf, container, inventorySlotsSize);
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

	@Nullable
	private static List<Slot> readSlots(FriendlyByteBuf buf, AbstractContainerMenu container, int slotCount) {
		if (slotCount > container.slots.size()) {
			LOGGER.error(
				"Recipe transfer packet has too many slot ids {} for container {} with {} slots",
				slotCount,
				container.getClass(),
				container.slots.size()
			);
			return null;
		}

		List<Slot> slots = new ArrayList<>(slotCount);
		for (int i = 0; i < slotCount; i++) {
			int slotIndex = buf.readVarInt();
			if (slotIndex < 0 || slotIndex >= container.slots.size()) {
				LOGGER.error(
					"Recipe transfer packet has invalid slot id {} for container {}",
					slotIndex,
					container.getClass()
				);
				return null;
			}
			Slot slot = container.getSlot(slotIndex);
			slots.add(slot);
		}
		return slots;
	}

}
