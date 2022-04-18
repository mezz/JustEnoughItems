package mezz.jei.common.network;

import mezz.jei.common.network.packets.IServerPacketHandler;
import mezz.jei.common.network.packets.PacketDeletePlayerItem;
import mezz.jei.common.network.packets.PacketGiveItemStack;
import mezz.jei.common.network.packets.PacketRecipeTransfer;
import mezz.jei.common.network.packets.PacketRequestCheatPermission;
import mezz.jei.common.network.packets.PacketSetHotbarItemStack;
import mezz.jei.core.config.IServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;

public class ServerPacketRouter {
	private static final Logger LOGGER = LogManager.getLogger();

	public final EnumMap<PacketIdServer, IServerPacketHandler> handlers = new EnumMap<>(PacketIdServer.class);
	private final IConnectionToClient connection;
	private final IServerConfig serverConfig;

	public ServerPacketRouter(IConnectionToClient connection, IServerConfig serverConfig) {
		this.connection = connection;
		this.serverConfig = serverConfig;
		handlers.put(PacketIdServer.RECIPE_TRANSFER, PacketRecipeTransfer::readPacketData);
		handlers.put(PacketIdServer.DELETE_ITEM, PacketDeletePlayerItem::readPacketData);
		handlers.put(PacketIdServer.GIVE_ITEM, PacketGiveItemStack::readPacketData);
		handlers.put(PacketIdServer.SET_HOTBAR_ITEM, PacketSetHotbarItemStack::readPacketData);
		handlers.put(PacketIdServer.CHEAT_PERMISSION_REQUEST, PacketRequestCheatPermission::readPacketData);
	}

	public void onPacket(FriendlyByteBuf packetBuffer, ServerPlayer player) {
		PacketIdServer packetId = null;
		try {
			int packetIdOrdinal = packetBuffer.readByte();
			packetId = PacketIdServer.VALUES[packetIdOrdinal];
			IServerPacketHandler packetHandler = handlers.get(packetId);
			ServerPacketContext context = new ServerPacketContext(player, serverConfig, connection);
			ServerPacketData data = new ServerPacketData(packetBuffer, context);
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
