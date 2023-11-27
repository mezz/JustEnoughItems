package mezz.jei.neoforge.network;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.packets.PacketJei;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.INetworkDirection;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

public class ConnectionToClient implements IConnectionToClient {
	private final NetworkHandler networkHandler;

	public ConnectionToClient(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}

	@Override
	public void sendPacketToClient(PacketJei packet, ServerPlayer player) {
		Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
		Packet<?> payload = PlayNetworkDirection.PLAY_TO_CLIENT.buildPacket(new INetworkDirection.PacketData(packetData.getKey(), 0), networkHandler.getChannelId());
		player.connection.send(payload);
	}
}
