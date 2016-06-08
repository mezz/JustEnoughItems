package mezz.jei;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import mezz.jei.network.packets.PacketJEI;
import mezz.jei.util.Log;

public class ProxyCommon {

	public void preInit(@Nonnull FMLPreInitializationEvent event) {

	}

	public void init(@Nonnull FMLInitializationEvent event) {

	}

	public void postInit(@Nonnull FMLPostInitializationEvent event) {

	}

	public void restartJEI() {

	}

	public void resetItemFilter() {

	}

	public void sendPacketToServer(PacketJEI packet) {
		Log.error("Tried to send packet to the server from the server: {}", packet);
	}

	public void sendPacketToPlayer(PacketJEI packet, EntityPlayer entityplayer) {
		if (!(entityplayer instanceof EntityPlayerMP) || (entityplayer instanceof FakePlayer)) {
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP) entityplayer;
		JustEnoughItems.getPacketHandler().sendPacket(packet.getPacket(), player);
	}
}
