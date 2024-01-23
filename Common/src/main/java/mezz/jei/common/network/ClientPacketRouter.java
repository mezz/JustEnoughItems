package mezz.jei.common.network;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.packets.IClientPacketHandler;
import mezz.jei.common.network.packets.PacketCheatPermission;
import mezz.jei.common.network.packets.PacketJeiToClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.Optional;

public class ClientPacketRouter {
	private static final Logger LOGGER = LogManager.getLogger();

	public final EnumMap<PacketIdClient, IClientPacketHandler> clientHandlers = new EnumMap<>(PacketIdClient.class);
	private final IConnectionToServer connection;
	private final IServerConfig serverConfig;

	public ClientPacketRouter(IConnectionToServer connection, IServerConfig serverConfig) {
		this.connection = connection;
		this.serverConfig = serverConfig;
		clientHandlers.put(PacketIdClient.CHEAT_PERMISSION, PacketCheatPermission::readPacketData);
	}

	public void onPacket(FriendlyByteBuf packetBuffer, LocalPlayer player) {
		getPacketId(packetBuffer)
			.ifPresent(packetId -> {
				IClientPacketHandler packetHandler = clientHandlers.get(packetId);
				ClientPacketContext context = new ClientPacketContext(player, connection, serverConfig);
				try {
					PacketJeiToClient packet = packetHandler.readPacketData(packetBuffer);
					if (packet != null) {
						Minecraft.getInstance().submit(() -> packet.processOnClientThread(context))
							.exceptionally(e -> {
								LOGGER.error("Packet error while executing packet on the client thread: {}", packetId.name(), e);
								return null;
							});
					}
				} catch (Throwable e) {
					LOGGER.error("Packet error when reading packet: {}", packetId.name(), e);
				}
			});
	}

	private Optional<PacketIdClient> getPacketId(FriendlyByteBuf packetBuffer) {
		try {
			int packetIdOrdinal = packetBuffer.readByte();
			PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
			return Optional.of(packetId);
		} catch (RuntimeException e) {
			LOGGER.error("Packet error when trying to read packet id", e);
			return Optional.empty();
		}
	}
}
