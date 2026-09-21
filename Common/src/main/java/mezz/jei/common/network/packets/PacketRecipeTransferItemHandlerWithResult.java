package mezz.jei.common.network.packets;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.transfer.BasicRecipeTransferHandlerServer;
import mezz.jei.common.transfer.RecipeTransferRequirement;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class PacketRecipeTransferItemHandlerWithResult extends PlayToServerPacket<PacketRecipeTransferItemHandlerWithResult> {
	private static final int MAX_SLOT_IDS = 1024;
	public static final CustomPacketPayload.Type<PacketRecipeTransferItemHandlerWithResult> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "recipe_transfer_item_handler_with_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferItemHandlerWithResult> STREAM_CODEC = StreamCodec.composite(
		RecipeTransferRequirement.STREAM_CODEC.apply(ByteBufCodecs.list(RecipeTransferRequirement.MAX_REQUIREMENTS)),
		p -> p.requirements,
		ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(MAX_SLOT_IDS)),
		p -> p.craftingSlots,
		ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(MAX_SLOT_IDS)),
		p -> p.inventorySlots,
		ByteBufCodecs.BOOL,
		p -> p.maxTransfer,
		ByteBufCodecs.BOOL,
		p -> p.requireCompleteSets,
		ByteBufCodecs.VAR_INT,
		p -> p.transferId,
		PacketRecipeTransferItemHandlerWithResult::new
	);

	private final List<RecipeTransferRequirement> requirements;
	private final List<Integer> craftingSlots;
	private final List<Integer> inventorySlots;
	private final boolean maxTransfer;
	private final boolean requireCompleteSets;
	private final int transferId;

	public static PacketRecipeTransferItemHandlerWithResult fromSlots(
		List<RecipeTransferRequirement> requirements,
		List<Slot> craftingSlots,
		List<Slot> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets,
		int transferId
	) {
		return new PacketRecipeTransferItemHandlerWithResult(
			requirements,
			craftingSlots.stream().map(s -> s.index).toList(),
			inventorySlots.stream().map(s -> s.index).toList(),
			maxTransfer,
			requireCompleteSets,
			transferId
		);
	}

	public PacketRecipeTransferItemHandlerWithResult(
		List<RecipeTransferRequirement> requirements,
		List<Integer> craftingSlots,
		List<Integer> inventorySlots,
		boolean maxTransfer,
		boolean requireCompleteSets,
		int transferId
	) {
		this.requirements = List.copyOf(requirements);
		this.craftingSlots = List.copyOf(craftingSlots);
		this.inventorySlots = List.copyOf(inventorySlots);
		this.maxTransfer = maxTransfer;
		this.requireCompleteSets = requireCompleteSets;
		this.transferId = transferId;
	}

	@Override
	public Type<PacketRecipeTransferItemHandlerWithResult> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferItemHandlerWithResult> streamCodec() {
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

		boolean successful = BasicRecipeTransferHandlerServer.setItemsFromItemHandlersWithResult(
			context.player(),
			requirements,
			craftingSlots,
			inventorySlots,
			maxTransfer,
			requireCompleteSets
		);
		PacketRecipeTransferWithResult.sendResult(context, transferId, successful);
	}
}
