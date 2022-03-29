package mezz.jei.network;

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

import java.util.EnumMap;

public class ClientPacketRouter {
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
		int packetIdOrdinal = packetBuffer.readByte();
		PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
		IClientPacketHandler packetHandler = clientHandlers.get(packetId);
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player != null) {
			ClientPacketData data = new ClientPacketData(packetBuffer, player, connection, serverConfig, worldConfig);
			packetHandler.readPacketData(data);
		}
	}
}
