package mezz.jei.network;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumMap;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;

import mezz.jei.config.Constants;
import mezz.jei.network.packets.IPacketJeiHandler;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.network.packets.PacketSetHotbarItemStack;
import mezz.jei.util.Log;

public class PacketHandler {
	public static final String CHANNEL_ID = Constants.MOD_ID;

	public final EnumMap<PacketIdServer, IPacketJeiHandler> serverHandlers = new EnumMap<>(PacketIdServer.class);

	public PacketHandler() {
		serverHandlers.put(PacketIdServer.RECIPE_TRANSFER, PacketRecipeTransfer::readPacketData);
		serverHandlers.put(PacketIdServer.DELETE_ITEM, PacketDeletePlayerItem::readPacketData);
		serverHandlers.put(PacketIdServer.GIVE_ITEM, PacketGiveItemStack::readPacketData);
		serverHandlers.put(PacketIdServer.SET_HOTBAR_ITEM, PacketSetHotbarItemStack::readPacketData);
		serverHandlers.put(PacketIdServer.CHEAT_PERMISSION_REQUEST, PacketRequestCheatPermission::readPacketData);
	}

	@SubscribeEvent
	public void onPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer packetBuffer = new PacketBuffer(event.getPacket().payload());
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;

		try {
			byte packetIdOrdinal = packetBuffer.readByte();
			PacketIdServer packetId = PacketIdServer.VALUES[packetIdOrdinal];
			IPacketJeiHandler packetHandler = serverHandlers.get(packetId);
			checkThreadAndEnqueue(packetHandler, packetBuffer, player, player.getServer());
		} catch (RuntimeException ex) {
			Log.get().error("Packet error", ex);
		}
	}

	private static void checkThreadAndEnqueue(IPacketJeiHandler packetHandler, PacketBuffer packetBuffer, EntityPlayer player, @Nullable IThreadListener threadListener) {
		if (threadListener != null && !threadListener.isCallingFromMinecraftThread()) {
			packetBuffer.retain();
			threadListener.addScheduledTask(() -> {
				try {
					packetHandler.readPacketData(packetBuffer, player);
					packetBuffer.release();
				} catch (IOException e) {
					Log.get().error("Network Error", e);
				}
			});
		}
	}
}
