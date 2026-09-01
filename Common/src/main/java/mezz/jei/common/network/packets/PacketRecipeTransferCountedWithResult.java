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

import java.util.List;

public class PacketRecipeTransferCountedWithResult extends PlayToServerPacket<PacketRecipeTransferCountedWithResult> {
	public static final CustomPacketPayload.Type<PacketRecipeTransferCountedWithResult> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "recipe_transfer_counted_with_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferCountedWithResult> STREAM_CODEC = StreamCodec.composite(
		TransferOperation.COUNTED_STREAM_CODEC.apply(ByteBufCodecs.list()),
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
		PacketRecipeTransferCountedWithResult::new
	);

	private final List<TransferOperation> transferOperations;
	private final List<Integer> craftingSlots;
	private final List<Integer> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;
	private final int transferId;

	public static PacketRecipeTransferCountedWithResult fromSlots(
		List<TransferOperation> transferOperations,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets,
		int transferId
	) {
		return new PacketRecipeTransferCountedWithResult(
			transferOperations,
			craftingSlots.stream().map(s -> s.index).toList(),
			inventorySlots.stream().map(s -> s.index).toList(),
			maxTransfer,
			requireCompleteSets,
			transferId
		);
	}

	public PacketRecipeTransferCountedWithResult(
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
	public Type<PacketRecipeTransferCountedWithResult> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferCountedWithResult> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ServerPacketContext context) {
		AbstractContainerMenu container = context.player().containerMenu;
		List<Slot> craftingSlots = PacketRecipeTransferWithResult.getSlots(container, this.craftingSlots);
		List<Slot> inventorySlots = PacketRecipeTransferWithResult.getSlots(container, this.inventorySlots);
		if (craftingSlots == null || inventorySlots == null) {
			PacketRecipeTransferWithResult.sendResult(context, transferId, false);
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
		PacketRecipeTransferWithResult.sendResult(context, transferId, successful);
	}
}
