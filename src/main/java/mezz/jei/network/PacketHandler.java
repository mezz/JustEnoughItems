package mezz.jei.network;

import javax.annotation.Nullable;
import java.io.IOException;

import mezz.jei.network.packets.IPacketJeiHandler;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.util.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PacketHandler {
	public static final String CHANNEL_ID = "JEI";

	@SubscribeEvent
	public void onPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer packetBuffer = new PacketBuffer(event.getPacket().payload());
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).playerEntity;

		try {
			byte packetIdOrdinal = packetBuffer.readByte();
			PacketIdServer packetId = PacketIdServer.VALUES[packetIdOrdinal];
			IPacketJeiHandler packetHandler;

			switch (packetId) {
				case RECIPE_TRANSFER: {
					packetHandler = new PacketRecipeTransfer.Handler();
					break;
				}
				case DELETE_ITEM: {
					packetHandler = new PacketDeletePlayerItem.Handler();
					break;
				}
				case GIVE_BIG: {
					packetHandler = new PacketGiveItemStack.Handler();
					break;
				}
				default: {
					return;
				}
			}

			checkThreadAndEnqueue(packetHandler, packetBuffer, player, player.getServer());
		} catch (RuntimeException ex) {
			Log.error("Packet error", ex);
		}
	}

	private static void checkThreadAndEnqueue(final IPacketJeiHandler packetHandler, final PacketBuffer packetBuffer, final EntityPlayer player, @Nullable IThreadListener threadListener) {
		if (threadListener != null && !threadListener.isCallingFromMinecraftThread()) {
			threadListener.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					try {
						packetHandler.readPacketData(packetBuffer, player);
					} catch (IOException e) {
						Log.error("Network Error", e);
					}
				}
			});
		}
	}
}
