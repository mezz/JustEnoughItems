package mezz.jei.config;

import com.google.common.collect.ImmutableMap;
import mezz.jei.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class ServerInfo {
	@Nullable
	private static UUID jeiOnServerCacheUuid = null;
	private static boolean jeiOnServerCacheValue = false;

	private ServerInfo() {

	}

	public static boolean isJeiOnServer() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener clientPacketListener = minecraft.getConnection();
		if (clientPacketListener == null) {
			return false;
		}
		UUID id = clientPacketListener.getId();
		if (!id.equals(jeiOnServerCacheUuid)) {
			jeiOnServerCacheUuid = id;
			jeiOnServerCacheValue = Optional.of(clientPacketListener)
				.map(ClientPacketListener::getConnection)
				.map(NetworkHooks::getConnectionData)
				.map(ConnectionData::getChannels)
				.map(ImmutableMap::keySet)
				.map(keys -> keys.contains(PacketHandler.CHANNEL_ID))
				.orElse(false);
		}
		return jeiOnServerCacheValue;
	}
}
