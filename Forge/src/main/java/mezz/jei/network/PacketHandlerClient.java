package mezz.jei.network;

import java.util.EnumMap;

import mezz.jei.common.network.PacketIdClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;

import mezz.jei.core.config.IWorldConfig;
import mezz.jei.network.packets.IPacketJeiHandler;
import mezz.jei.network.packets.PacketCheatPermission;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PacketHandlerClient {
	private static final Logger LOGGER = LogManager.getLogger();
	public final EnumMap<PacketIdClient, IPacketJeiHandler> clientHandlers = new EnumMap<>(PacketIdClient.class);

	public PacketHandlerClient(IWorldConfig worldConfig) {
		clientHandlers.put(PacketIdClient.CHEAT_PERMISSION, (buf, player) -> PacketCheatPermission.readPacketData(buf, player, worldConfig));
	}

	public void onPacket(NetworkEvent.ServerCustomPayloadEvent event) {
		try {
			FriendlyByteBuf packetBuffer = new FriendlyByteBuf(event.getPayload());
			int packetIdOrdinal = packetBuffer.readByte();
			PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
			IPacketJeiHandler packetHandler = clientHandlers.get(packetId);
			Minecraft minecraft = Minecraft.getInstance();
			Player player = minecraft.player;
			if (player != null) {
				packetHandler.readPacketData(packetBuffer, player);
			}
		} catch (Throwable e) {
			LOGGER.error("Packet error", e);
		}
		event.getSource().get().setPacketHandled(true);
	}
}
