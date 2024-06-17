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

public class PacketRecipeTransfer extends PlayToServerPacket<PacketRecipeTransfer> {
	public static final CustomPacketPayload.Type<PacketRecipeTransfer> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "recipe_transfer"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransfer> STREAM_CODEC = StreamCodec.composite(
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
		PacketRecipeTransfer::new
	);

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
	public Type<PacketRecipeTransfer> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransfer> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ServerPacketContext context) {
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

}
