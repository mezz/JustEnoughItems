package mezz.jei.network;

import java.util.EnumMap;

import mezz.jei.common.network.PacketIdServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.constants.ModIds;
import mezz.jei.network.packets.IPacketJeiHandler;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.network.packets.PacketSetHotbarItemStack;
import net.minecraftforge.network.NetworkEvent;
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
		FriendlyByteBuf packetBuffer = new FriendlyByteBuf(event.getPayload());
		NetworkEvent.Context context = event.getSource().get();
		ServerPlayer player = context.getSender();
		if (player == null) {
			LOGGER.error("Packet error, the sender player is missing for event: {}", event);
			return;
		}
		try {
			int packetIdOrdinal = packetBuffer.readByte();
			PacketIdServer packetId = PacketIdServer.VALUES[packetIdOrdinal];
			IPacketJeiHandler packetHandler = serverHandlers.get(packetId);
			packetHandler.readPacketData(packetBuffer, player);
		} catch (Throwable e) {
			LOGGER.error("Packet error for event: {}", event, e);
		}
		event.getSource().get().setPacketHandled(true);
	}
}
