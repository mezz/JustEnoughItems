package mezz.jei.forge.network;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.packets.PacketJei;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ICustomPacket;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

public class ConnectionToClient implements IConnectionToClient {
	private final NetworkHandler networkHandler;

	public ConnectionToClient(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}

	@Override
	public void sendPacketToClient(PacketJei packet, ServerPlayer player) {
		Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
		ICustomPacket<Packet<?>> payload = NetworkDirection.PLAY_TO_CLIENT.buildPacket(packetData, networkHandler.getChannelId());
		player.connection.send(payload.getThis());
	}
}
