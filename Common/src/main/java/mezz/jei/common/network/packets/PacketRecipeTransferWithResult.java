package mezz.jei.common.network.packets;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PacketRecipeTransferWithResult extends PlayToServerPacket<PacketRecipeTransferWithResult> {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final CustomPacketPayload.Type<PacketRecipeTransferWithResult> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "recipe_transfer_with_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferWithResult> STREAM_CODEC = StreamCodec.composite(
		TransferOperation.STREAM_CODEC.apply(ByteBufCodecs.list()),
		p -> p.transferOperations,
		ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
		p -> p.craftingSlots,
		ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
		p -> p.inventorySlots,
		ByteBufCodecs.BOOL,
		p -> p.maxTransfer,
		ByteBufCodecs.BOOL,
		p -> p.requireCompleteSets,
		ByteBufCodecs.VAR_INT,
		p -> p.transferId,
		PacketRecipeTransferWithResult::new
	);

	private final List<TransferOperation> transferOperations;
	private final List<Integer> craftingSlots;
	private final List<Integer> inventorySlots;
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
			craftingSlots.stream().map(s -> s.index).toList(),
			inventorySlots.stream().map(s -> s.index).toList(),
			maxTransfer,
			requireCompleteSets,
			transferId
		);
	}

	public PacketRecipeTransferWithResult(
		List<TransferOperation> transferOperations,
		List<Integer> craftingSlots,
		List<Integer> inventorySlots,
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
	public Type<PacketRecipeTransferWithResult> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferWithResult> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ServerPacketContext context) {
		AbstractContainerMenu container = context.player().containerMenu;
		List<Slot> craftingSlots = getSlots(container, this.craftingSlots);
		List<Slot> inventorySlots = getSlots(container, this.inventorySlots);
		if (craftingSlots == null || inventorySlots == null) {
			sendResult(context, transferId, false);
			return;
		}

		boolean successful = BasicRecipeTransferHandlerServer.setItemsWithResult(
			context.player(),
			transferOperations,
			craftingSlots,
			inventorySlots,
			maxTransfer,
			requireCompleteSets
		);
		sendResult(context, transferId, successful);
	}

	static void sendResult(ServerPacketContext context, int transferId, boolean successful) {
		context.connection().sendPacketToClient(
			new PacketRecipeTransferResult(transferId, successful),
			context.player()
		);
	}

	@Nullable
	public static List<Slot> getSlots(AbstractContainerMenu container, List<Integer> slotIds) {
		if (slotIds.size() > container.slots.size()) {
			LOGGER.error(
				"Recipe transfer packet has too many slot ids {} for container {} with {} slots",
				slotIds.size(),
				container.getClass(),
				container.slots.size()
			);
			return null;
		}

		List<Slot> slots = new ArrayList<>(slotIds.size());
		for (int slotId : slotIds) {
			if (slotId < 0 || slotId >= container.slots.size()) {
				LOGGER.error(
					"Recipe transfer packet has invalid slot id {} for container {}",
					slotId,
					container.getClass()
				);
				return null;
			}
			slots.add(container.getSlot(slotId));
		}
		return slots;
	}
}
