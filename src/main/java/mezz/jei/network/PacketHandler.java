package mezz.jei.network;

import java.util.EnumMap;

import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.ModIds;
import mezz.jei.network.packets.IPacketJeiHandler;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.network.packets.PacketSetHotbarItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final ResourceLocation CHANNEL_ID = new ResourceLocation(ModIds.JEI_ID, "channel");

	public final EnumMap<PacketIdServer, IPacketJeiHandler> serverHandlers = new EnumMap<>(PacketIdServer.class);

	public PacketHandler() {
		serverHandlers.put(PacketIdServer.RECIPE_TRANSFER, PacketRecipeTransfer::readPacketData);
		serverHandlers.put(PacketIdServer.DELETE_ITEM, PacketDeletePlayerItem::readPacketData);
		serverHandlers.put(PacketIdServer.GIVE_ITEM, PacketGiveItemStack::readPacketData);
		serverHandlers.put(PacketIdServer.SET_HOTBAR_ITEM, PacketSetHotbarItemStack::readPacketData);
		serverHandlers.put(PacketIdServer.CHEAT_PERMISSION_REQUEST, PacketRequestCheatPermission::readPacketData);
	}

	public void onPacket(NetworkEvent.ClientCustomPayloadEvent event) {
		PacketBuffer packetBuffer = new PacketBuffer(event.getPayload());
		NetworkEvent.Context context = event.getSource().get();
		ServerPlayerEntity player = context.getSender();
		if (player == null) {
			LOGGER.error("Packet error, the sender player is missing for event: {}", event);
			return;
		}
		try {
			int packetIdOrdinal = packetBuffer.readByte();
			PacketIdServer packetId = PacketIdServer.VALUES[packetIdOrdinal];
			IPacketJeiHandler packetHandler = serverHandlers.get(packetId);
			packetHandler.readPacketData(packetBuffer, player);
		} catch (RuntimeException e) {
			LOGGER.error("Packet error for event: {}", event, e);
		}
		event.getSource().get().setPacketHandled(true);
	}
}
