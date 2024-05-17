package mezz.jei.forge.network;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.packets.PlayToClientPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.NetworkDirection;

public class ConnectionToClient implements IConnectionToClient {
	private final NetworkHandler networkHandler;

	public ConnectionToClient(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}

	@Override
	public <T extends PlayToClientPacket<T>> void sendPacketToClient(T packet, ServerPlayer player) {
		Channel<CustomPacketPayload> channel = networkHandler.getChannel();
		Packet<?> payload = NetworkDirection.PLAY_TO_CLIENT.buildPacket(channel, packet);
		player.connection.send(payload);
	}
}
