package mezz.jei;

import mezz.jei.network.packets.PacketJei;
import mezz.jei.util.Log;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ProxyCommon {

	public void preInit(FMLPreInitializationEvent event) {

	}

	public void init(FMLInitializationEvent event) {

	}

	public void loadComplete(FMLLoadCompleteEvent event) {

	}

	public void restartJEI() {

	}

	public void sendPacketToServer(PacketJei packet) {
		Log.error("Tried to send packet to the server from the server: {}", packet);
	}
}
