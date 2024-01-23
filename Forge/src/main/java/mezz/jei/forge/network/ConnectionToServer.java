package mezz.jei.forge.network;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PacketJeiToServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.network.EventNetworkChannel;
import net.minecraftforge.network.ICustomPacket;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ConnectionToServer implements IConnectionToServer {
	@Nullable
	private static UUID jeiOnServerCacheUuid = null;
	private static boolean jeiOnServerCacheValue = false;
	private final NetworkHandler networkHandler;

	public ConnectionToServer(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}

	@Override
	public boolean isJeiOnServer() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener clientPacketListener = minecraft.getConnection();
		if (clientPacketListener == null) {
			return false;
		}
		UUID id = clientPacketListener.getId();
		if (!id.equals(jeiOnServerCacheUuid)) {
			jeiOnServerCacheUuid = id;
			Connection connection = clientPacketListener.getConnection();
			EventNetworkChannel networkChannel = networkHandler.getChannel();
			jeiOnServerCacheValue = networkChannel.isRemotePresent(connection);
		}
		return jeiOnServerCacheValue;
	}

	@Override
	public void sendPacketToServer(PacketJeiToServer packet) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener netHandler = minecraft.getConnection();
		if (netHandler != null && isJeiOnServer()) {
			Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
			ICustomPacket<Packet<?>> payload = NetworkDirection.PLAY_TO_SERVER.buildPacket(packetData.getKey(), networkHandler.getChannelId());
			netHandler.send(payload.getThis());
		}
	}
}
