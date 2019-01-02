package mezz.jei.startup;

import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

import mezz.jei.network.PacketHandler;

public class ServerLifecycleHandler {
	public ServerLifecycleHandler() {
		EventNetworkChannel channel = NetworkRegistry.newEventChannel(PacketHandler.CHANNEL_ID, () -> "1.0.0", s -> true, s -> true);
		PacketHandler packetHandler = new PacketHandler();
		channel.addListener(packetHandler::onPacket);
	}
}
