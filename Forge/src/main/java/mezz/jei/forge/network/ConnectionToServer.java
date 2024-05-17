package mezz.jei.forge.network;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PlayToServerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.NetworkDirection;
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
			Channel<CustomPacketPayload> channel = networkHandler.getChannel();
			jeiOnServerCacheValue = channel.isRemotePresent(connection);
		}
		return jeiOnServerCacheValue;
	}

	@Override
	public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener netHandler = minecraft.getConnection();
		if (netHandler != null && isJeiOnServer()) {
			Channel<CustomPacketPayload> channel = networkHandler.getChannel();
			Packet<?> payload = NetworkDirection.PLAY_TO_SERVER.buildPacket(channel, packet);
			netHandler.send(payload);
		}
	}
}
