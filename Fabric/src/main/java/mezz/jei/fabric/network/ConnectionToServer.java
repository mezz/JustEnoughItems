package mezz.jei.fabric.network;

import mezz.jei.common.Constants;
import mezz.jei.common.network.ClientConnectionHelper;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketJei;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.tuple.Pair;

public final class ConnectionToServer implements IConnectionToServer {
	private static final String FABRIC_SERVER_BRAND = "fabric";

	@Override
	public boolean isJeiOnServer() {
		return ClientPlayNetworking.canSend(Constants.NETWORK_CHANNEL_ID);
	}

	@Override
	public boolean isSameModLoader() {
		return ClientConnectionHelper.hasServerBrand(FABRIC_SERVER_BRAND);
	}

	@Override
	public boolean supportsRecipeTransferResults() {
		return ClientPlayNetworking.canSend(Constants.RECIPE_TRANSFER_RESULT_CHANNEL_ID);
	}

	@Override
	public void sendPacketToServer(PacketJei packet) {
		if (isJeiOnServer()) {
			Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
			FriendlyByteBuf buf = packetData.getLeft();
			ClientPlayNetworking.send(packet.getChannelId(), buf);
		}
	}
}
