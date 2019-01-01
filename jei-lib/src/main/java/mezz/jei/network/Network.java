package mezz.jei.network;

import net.minecraftforge.fml.network.ICustomPacket;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

import mezz.jei.config.ServerInfo;
import mezz.jei.network.packets.PacketJei;
import org.apache.commons.lang3.tuple.Pair;

public class Network {
	@OnlyIn(Dist.CLIENT)
	public static void sendPacketToServer(PacketJei packet) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null) {
			NetHandlerPlayClient netHandler = minecraft.getConnection();
			if (netHandler != null && ServerInfo.isJeiOnServer()) {
				Pair<PacketBuffer, Integer> packetData = packet.getPacketData();
				ICustomPacket<Packet<?>> payload = NetworkDirection.PLAY_TO_SERVER.buildPacket(packetData, PacketHandler.CHANNEL_ID);
				netHandler.sendPacket(payload.getThis());
			}
		}
	}

	public static void sendPacketToClient(PacketJei packet, EntityPlayerMP player) {
		Pair<PacketBuffer, Integer> packetData = packet.getPacketData();
		ICustomPacket<Packet<?>> payload = NetworkDirection.PLAY_TO_CLIENT.buildPacket(packetData, PacketHandler.CHANNEL_ID);
		player.connection.sendPacket(payload.getThis());
	}
}
