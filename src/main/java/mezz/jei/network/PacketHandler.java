package mezz.jei.network;

import javax.annotation.Nullable;
import java.io.IOException;

import mezz.jei.config.Constants;
import mezz.jei.network.packets.IPacketJeiHandler;
import mezz.jei.network.packets.PacketCheatPermission;
import mezz.jei.network.packets.PacketDeletePlayerItem;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketHandler {
	public static final String CHANNEL_ID = Constants.MOD_ID;

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
				case CHEAT_PERMISSION_REQUEST: {
					packetHandler = new PacketRequestCheatPermission.Handler();
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
		PacketBuffer packetBuffer = new PacketBuffer(event.getPacket().payload());
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.player;
		IPacketJeiHandler packetHandler;

		try {
			byte packetIdOrdinal = packetBuffer.readByte();
			PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
			switch (packetId) {
				case CHEAT_PERMISSION: {
					packetHandler = new PacketCheatPermission.Handler();
					break;
				}
				default: {
					return;
				}
			}

			checkThreadAndEnqueue(packetHandler, packetBuffer, player, minecraft);
		} catch (Exception ex) {
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
