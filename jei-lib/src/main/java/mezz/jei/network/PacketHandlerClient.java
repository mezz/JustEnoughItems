package mezz.jei.network;

import java.util.EnumMap;

import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import mezz.jei.config.WorldConfig;
import mezz.jei.network.packets.IPacketJeiHandler;
import mezz.jei.network.packets.PacketCheatPermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PacketHandlerClient {
	private static final Logger LOGGER = LogManager.getLogger();
	public final EnumMap<PacketIdClient, IPacketJeiHandler> clientHandlers = new EnumMap<>(PacketIdClient.class);

	public PacketHandlerClient(WorldConfig worldConfig) {
		clientHandlers.put(PacketIdClient.CHEAT_PERMISSION, (buf, player) -> PacketCheatPermission.readPacketData(buf, player, worldConfig));
	}

	public void onPacket(NetworkEvent.ClientCustomPayloadEvent event) {
		try {
			PacketBuffer packetBuffer = new PacketBuffer(event.getPayload());
			int packetIdOrdinal = event.getLoginIndex();
			PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
			IPacketJeiHandler packetHandler = clientHandlers.get(packetId);
			Minecraft minecraft = Minecraft.getInstance();
			EntityPlayer player = minecraft.player;
			if (player != null) {
				packetHandler.readPacketData(packetBuffer, player);
			}
		} catch (Exception e) {
			LOGGER.error("Packet error", e);
		}
	}
}
