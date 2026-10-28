package mezz.jei.common.network.packets;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.transfer.PendingRecipeTransfers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class PacketRecipeTransferResult extends PlayToClientPacket<PacketRecipeTransferResult> {
	public static final Type<PacketRecipeTransferResult> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "recipe_transfer_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferResult> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		p -> p.transferId,
		ByteBufCodecs.BOOL,
		p -> p.result == RecipeTransferResult.SUCCESS,
		PacketRecipeTransferResult::new
	);

	public final int transferId;
	public final RecipeTransferResult result;

	public PacketRecipeTransferResult(int transferId, boolean successful) {
		this.transferId = transferId;
		if (successful) {
			this.result = RecipeTransferResult.SUCCESS;
		} else {
			this.result = RecipeTransferResult.REJECTED;
		}
	}

	@Override
	public Type<PacketRecipeTransferResult> type() {
		return TYPE;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, PacketRecipeTransferResult> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public void process(ClientPacketContext context) {
		completePendingRecipeTransfer();
	}

	public void completePendingRecipeTransfer() {
		PendingRecipeTransfers.INSTANCE.completeRecipeTransfer(transferId, result);
	}
}
