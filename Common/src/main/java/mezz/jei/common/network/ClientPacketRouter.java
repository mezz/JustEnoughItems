package mezz.jei.common.network;

import mezz.jei.common.network.packets.IClientPacketHandler;
import mezz.jei.common.network.packets.PacketCheatPermission;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.core.config.IWorldConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;

public class ClientPacketRouter {
	private static final Logger LOGGER = LogManager.getLogger();

	public final EnumMap<PacketIdClient, IClientPacketHandler> clientHandlers = new EnumMap<>(PacketIdClient.class);
	private final IConnectionToServer connection;
	private final IServerConfig serverConfig;
	private final IWorldConfig worldConfig;

	public ClientPacketRouter(IConnectionToServer connection, IServerConfig serverConfig, IWorldConfig worldConfig) {
		this.connection = connection;
		this.serverConfig = serverConfig;
		this.worldConfig = worldConfig;
		clientHandlers.put(PacketIdClient.CHEAT_PERMISSION, PacketCheatPermission::readPacketData);
	}

	public void onPacket(FriendlyByteBuf packetBuffer, LocalPlayer player) {
		PacketIdClient packetId = null;
		try {
			int packetIdOrdinal = packetBuffer.readByte();
			packetId = PacketIdClient.VALUES[packetIdOrdinal];
			IClientPacketHandler packetHandler = clientHandlers.get(packetId);
			ClientPacketContext context = new ClientPacketContext(player, connection, serverConfig, worldConfig);
			ClientPacketData data = new ClientPacketData(packetBuffer, context);
			packetHandler.readPacketData(data);
		} catch (Throwable e) {
			if (packetId != null) {
				LOGGER.error("Packet error when reading packet: {}", packetId.name(), e);
			} else {
				LOGGER.error("Packet error", e);
			}
		}
	}
}
