package mezz.jei;

import javax.annotation.Nonnull;
import java.util.Set;

import mezz.jei.util.ReflectionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import mezz.jei.api.JEIManager;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.util.Log;

public class ProxyCommon {

	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		ReflectionUtil.init();
	}

	public void init(@Nonnull FMLInitializationEvent event) {

	}

	public void startJEI(@Nonnull Set<ASMDataTable.ASMData> modPlugins) {
		JEIManager.itemRegistry = new ItemRegistryDummy();
		JEIManager.recipeRegistry = new RecipeRegistryDummy();
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
		JustEnoughItems.packetHandler.sendPacket(packet.getPacket(), player);
	}
}
