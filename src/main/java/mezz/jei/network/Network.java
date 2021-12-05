package mezz.jei.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;

import mezz.jei.config.ServerInfo;
import mezz.jei.network.packets.PacketJei;
import net.minecraftforge.network.ICustomPacket;
import net.minecraftforge.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

public class Network {
	@OnlyIn(Dist.CLIENT)
	public static void sendPacketToServer(PacketJei packet) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientPacketListener netHandler = minecraft.getConnection();
		if (netHandler != null && ServerInfo.isJeiOnServer()) {
			Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
			ICustomPacket<Packet<?>> payload = NetworkDirection.PLAY_TO_SERVER.buildPacket(packetData, PacketHandler.CHANNEL_ID);
			netHandler.send(payload.getThis());
		}
	}

	public static void sendPacketToClient(PacketJei packet, ServerPlayer player) {
		Pair<FriendlyByteBuf, Integer> packetData = packet.getPacketData();
		ICustomPacket<Packet<?>> payload = NetworkDirection.PLAY_TO_CLIENT.buildPacket(packetData, PacketHandler.CHANNEL_ID);
		player.connection.send(payload.getThis());
	}
}
