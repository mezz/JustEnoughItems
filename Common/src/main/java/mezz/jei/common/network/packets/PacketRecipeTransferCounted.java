package mezz.jei.common.network.packets;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class PacketRecipeTransferCounted extends PlayToServerPacket<PacketRecipeTransferCounted> {
	public static final CustomPacketPayload.Type<PacketRecipeTransferCounted> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "recipe_transfer_counted"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferCounted> STREAM_CODEC = StreamCodec.composite(
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
		PacketRecipeTransferCounted::new
	);

	public final List<TransferOperation> transferOperations;
	public final List<Integer> craftingSlots;
	public final List<Integer> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;

	public static PacketRecipeTransferCounted fromSlots(
		List<TransferOperation> transferOperations,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets
	) {
		return new PacketRecipeTransferCounted(
			transferOperations,
			craftingSlots.stream().map(s -> s.index).toList(),
			inventorySlots.stream().map(s -> s.index).toList(),
			maxTransfer,
			requireCompleteSets
		);
	}

	public PacketRecipeTransferCounted(
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
	public Type<PacketRecipeTransferCounted> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferCounted> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ServerPacketContext context) {
		AbstractContainerMenu container = context.player().containerMenu;
		List<Slot> craftingSlots = PacketRecipeTransfer.getSlots(container, this.craftingSlots);
		List<Slot> inventorySlots = PacketRecipeTransfer.getSlots(container, this.inventorySlots);
		if (craftingSlots == null || inventorySlots == null) {
			return;
		}

		BasicRecipeTransferHandlerServer.setItems(
			context.player(),
			transferOperations,
			craftingSlots,
			inventorySlots,
			maxTransfer,
			requireCompleteSets
		);
	}
}
