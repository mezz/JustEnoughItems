package mezz.jei.network;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumMap;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;

import mezz.jei.network.packets.IPacketJeiHandler;
import mezz.jei.network.packets.PacketCheatPermission;
import mezz.jei.util.Log;

@SideOnly(Side.CLIENT)
public class PacketHandlerClient extends PacketHandler {
	public final EnumMap<PacketIdClient, IPacketJeiHandler> clientHandlers = new EnumMap<>(PacketIdClient.class);

	public PacketHandlerClient() {
		clientHandlers.put(PacketIdClient.CHEAT_PERMISSION, PacketCheatPermission::readPacketData);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
		PacketBuffer packetBuffer = new PacketBuffer(event.getPacket().payload());
		Minecraft minecraft = Minecraft.getMinecraft();

		try {
			byte packetIdOrdinal = packetBuffer.readByte();
			PacketIdClient packetId = PacketIdClient.VALUES[packetIdOrdinal];
			IPacketJeiHandler packetHandler = clientHandlers.get(packetId);
			checkThreadAndEnqueue(packetHandler, packetBuffer, minecraft);
		} catch (Exception ex) {
			Log.get().error("Packet error", ex);
		}
	}

	private static void checkThreadAndEnqueue(final IPacketJeiHandler packetHandler, final PacketBuffer packetBuffer, @Nullable IThreadListener threadListener) {
		if (threadListener != null && !threadListener.isCallingFromMinecraftThread()) {
			packetBuffer.retain();
			threadListener.addScheduledTask(() -> {
				try {
					Minecraft minecraft = Minecraft.getMinecraft();
					EntityPlayer player = minecraft.player;
					if (player != null) {
						packetHandler.readPacketData(packetBuffer, player);
					}
					packetBuffer.release();
				} catch (IOException e) {
					Log.get().error("Network Error", e);
				}
			});
		}
	}
}
