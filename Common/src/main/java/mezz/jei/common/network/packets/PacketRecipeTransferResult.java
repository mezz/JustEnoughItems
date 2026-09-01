package mezz.jei.common.network.packets;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class PacketRecipeTransferResult extends PlayToClientPacket<PacketRecipeTransferResult> {
	private static final Map<Integer, IRecipeTransferContext<?, ?>> PENDING_RECIPE_TRANSFERS = new HashMap<>();
	public static final Type<PacketRecipeTransferResult> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "recipe_transfer_result"));
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

	public static void registerPendingRecipeTransfer(IRecipeTransferContext<?, ?> context) {
		ErrorUtil.checkNotNull(context, "context");
		PENDING_RECIPE_TRANSFERS.put(context.getTransferId(), context);
	}

	public static void clearPendingRecipeTransfers() {
		PENDING_RECIPE_TRANSFERS.clear();
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
		IRecipeTransferContext<?, ?> recipeTransferContext = PENDING_RECIPE_TRANSFERS.remove(transferId);
		if (recipeTransferContext != null) {
			recipeTransferContext.completeRecipeTransfer(result);
		}
	}
}
