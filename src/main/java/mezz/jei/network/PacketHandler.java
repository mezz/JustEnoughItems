package mezz.jei.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketGiveItemMessageBig;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.util.Log;

public class PacketHandler {
	public static final String CHANNEL_ID = "JEI";
	private final FMLEventChannel channel;

	public PacketHandler() {
		channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL_ID);
		channel.register(this);
	}

	@SubscribeEvent
	public void onPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		PacketBuffer packetBuffer = new PacketBuffer(event.getPacket().payload());
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).playerEntity;

		try {
			byte packetIdOrdinal = packetBuffer.readByte();
			PacketIdServer packetId = PacketIdServer.VALUES[packetIdOrdinal];
			PacketJEI packet;

			switch (packetId) {
				case RECIPE_TRANSFER: {
					packet = new PacketRecipeTransfer();
					break;
				}
				case DELETE_ITEM: {
					packet = new PacketDeletePlayerItem();
					break;
				}
				case GIVE_BIG: {
					packet = new PacketGiveItemMessageBig();
					break;
				}
				default: {
					return;
				}
			}

			checkThreadAndEnqueue(packet, packetBuffer, player, player.getServerForPlayer());
		} catch (RuntimeException ex) {
			Log.error("Packet error", ex);
		}
	}

	/*
	@SubscribeEvent
	public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
		PacketBuffer packetBuffer = new PacketBuffer(event.packet.payload());
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.thePlayer;
		PacketJEI packet;

		try {
			byte packetIdOrdinal = packetBuffer.readByte();
			PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
			switch (packetId) {
				default: {
					return;
				}
			}

			checkThreadAndEnqueue(packet, packetBuffer, player, minecraft);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	*/

	public void sendPacket(FMLProxyPacket packet, EntityPlayerMP player) {
		channel.sendTo(packet, player);
	}

	private static void checkThreadAndEnqueue(final PacketJEI packet, final PacketBuffer packetBuffer, final EntityPlayer player, IThreadListener threadListener) {
		if (!threadListener.isCallingFromMinecraftThread()) {
			threadListener.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					try {
						packet.readPacketData(packetBuffer, player);
					} catch (IOException e) {
						Log.error("Network Error", e);
					}
				}
			});
		}
	}
}
