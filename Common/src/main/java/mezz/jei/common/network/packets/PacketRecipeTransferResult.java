package mezz.jei.common.network.packets;

import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.common.Constants;
import mezz.jei.common.network.ClientPacketData;
import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdClient;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PacketRecipeTransferResult extends PacketJei {
	private static final Map<Integer, IRecipeTransferContext<?, ?>> PENDING_RECIPE_TRANSFERS = new HashMap<>();

	private final int transferId;
	private final RecipeTransferResult result;

	public PacketRecipeTransferResult(int transferId, boolean successful) {
		this.transferId = transferId;
		this.result = successful ? RecipeTransferResult.SUCCESS : RecipeTransferResult.REJECTED;
	}

	public static void registerPendingRecipeTransfer(IRecipeTransferContext<?, ?> context) {
		ErrorUtil.checkNotNull(context, "context");
		PENDING_RECIPE_TRANSFERS.put(context.getTransferId(), context);
	}

	public static void clearPendingRecipeTransfers() {
		PENDING_RECIPE_TRANSFERS.clear();
	}

	@Override
	public ResourceLocation getChannelId() {
		return Constants.RECIPE_TRANSFER_RESULT_CHANNEL_ID;
	}

	@Override
	protected IPacketId getPacketId() {
		return PacketIdClient.RECIPE_TRANSFER_RESULT;
	}

	@Override
	protected void writePacketData(FriendlyByteBuf buf) {
		buf.writeVarInt(transferId);
		buf.writeBoolean(result == RecipeTransferResult.SUCCESS);
	}

	public static CompletableFuture<Void> readPacketData(ClientPacketData data) {
		FriendlyByteBuf buf = data.buf();
		PacketRecipeTransferResult packet = new PacketRecipeTransferResult(buf.readVarInt(), buf.readBoolean());
		return Minecraft.getInstance().submit(packet::completePendingRecipeTransfer);
	}

	public void completePendingRecipeTransfer() {
		IRecipeTransferContext<?, ?> recipeTransferContext = PENDING_RECIPE_TRANSFERS.remove(transferId);
		if (recipeTransferContext != null) {
			recipeTransferContext.completeRecipeTransfer(result);
		}
	}
}
