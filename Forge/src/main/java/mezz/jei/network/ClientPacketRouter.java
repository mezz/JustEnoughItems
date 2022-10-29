package mezz.jei.network;

import mezz.jei.common.network.ClientPacketContext;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.PacketIdClient;
import mezz.jei.common.network.ClientPacketData;
import mezz.jei.common.network.packets.IClientPacketHandler;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.core.config.IWorldConfig;
import mezz.jei.common.network.packets.PacketCheatPermission;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
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

	public void onPacket(FriendlyByteBuf packetBuffer) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		getPacketId(packetBuffer)
			.ifPresent(packetId -> {
				IClientPacketHandler packetHandler = clientHandlers.get(packetId);
				ClientPacketContext context = new ClientPacketContext(player, connection, serverConfig, worldConfig);
				ClientPacketData data = new ClientPacketData(packetBuffer, context);
				try {
					packetHandler.readPacketData(data)
						.exceptionally(e -> {
							LOGGER.error("Packet error while executing packet on the client thread: {}", packetId.name(), e);
							return null;
						});
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
