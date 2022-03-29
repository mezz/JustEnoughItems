package mezz.jei.network;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.packets.IServerPacketHandler;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.network.packets.PacketSetHotbarItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;

public class ServerPacketRouter {
	public static final ResourceLocation CHANNEL_ID = new ResourceLocation(ModIds.JEI_ID, "channel");

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
		int packetIdOrdinal = packetBuffer.readByte();
		PacketIdServer packetId = PacketIdServer.VALUES[packetIdOrdinal];
		IServerPacketHandler packetHandler = handlers.get(packetId);

		ServerPacketData data = new ServerPacketData(packetBuffer, player, serverConfig, connection);
		packetHandler.readPacketData(data);
	}
}
