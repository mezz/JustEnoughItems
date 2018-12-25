package mezz.jei.startup;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

import mezz.jei.network.PacketHandler;

public class ServerLifecycleHandler {
	public ServerLifecycleHandler() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, FMLPreInitializationEvent.class, event -> this.preInit());
	}

	private void preInit() {
		EventNetworkChannel channel = NetworkRegistry.newEventChannel(PacketHandler.CHANNEL_ID, () -> "1.0.0", s -> true, s -> true);
		PacketHandler packetHandler = new PacketHandler();
		channel.addListener(packetHandler::onPacket);
	}
}
