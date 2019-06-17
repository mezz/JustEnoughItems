package mezz.jei.network;

import net.minecraftforge.fml.network.ICustomPacket;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

import mezz.jei.config.ServerInfo;
import mezz.jei.network.packets.PacketJei;
import org.apache.commons.lang3.tuple.Pair;

public class Network {
	@OnlyIn(Dist.CLIENT)
	public static void sendPacketToServer(PacketJei packet) {
		Minecraft minecraft = Minecraft.getInstance();
		//noinspection ConstantConditions
		if (minecraft != null) {
			ClientPlayNetHandler netHandler = minecraft.getConnection();
			if (netHandler != null && ServerInfo.isJeiOnServer()) {
				Pair<PacketBuffer, Integer> packetData = packet.getPacketData();
				ICustomPacket<IPacket<?>> payload = NetworkDirection.PLAY_TO_SERVER.buildPacket(packetData, PacketHandler.CHANNEL_ID);
				netHandler.sendPacket(payload.getThis());
			}
		}
	}

	public static void sendPacketToClient(PacketJei packet, ServerPlayerEntity player) {
		Pair<PacketBuffer, Integer> packetData = packet.getPacketData();
		ICustomPacket<IPacket<?>> payload = NetworkDirection.PLAY_TO_CLIENT.buildPacket(packetData, PacketHandler.CHANNEL_ID);
		player.connection.sendPacket(payload.getThis());
	}
}
