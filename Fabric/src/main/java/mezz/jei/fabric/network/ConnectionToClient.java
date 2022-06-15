package mezz.jei.fabric.network;

import mezz.jei.common.Constants;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.packets.PacketJei;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;

public class ConnectionToClient implements IConnectionToClient {
	@Override
	public void sendPacketToClient(PacketJei packet, ServerPlayer player) {
		Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
		FriendlyByteBuf buf = packetData.getLeft();
		ServerPlayNetworking.send(player, Constants.NETWORK_CHANNEL_ID, buf);
	}
}
