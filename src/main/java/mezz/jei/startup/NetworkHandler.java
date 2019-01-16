package mezz.jei.startup;

import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import mezz.jei.config.IWorldConfig;
import mezz.jei.config.ServerInfo;
import mezz.jei.network.PacketHandler;
import mezz.jei.network.PacketHandlerClient;

public class NetworkHandler {
	private final EventNetworkChannel channel;

	public NetworkHandler() {
		channel = NetworkRegistry.newEventChannel(PacketHandler.CHANNEL_ID, () -> "1.0.0", s -> {
			boolean jeiOnServer = !NetworkRegistry.ABSENT.equals(s);
			ServerInfo.onConnectedToServer(jeiOnServer);
			return true;
		}, s -> true);
	}

	public void createServerPacketHandler() {
		PacketHandler packetHandler = new PacketHandler();
		channel.addListener(packetHandler::onPacket);
	}

	@OnlyIn(Dist.CLIENT)
	public void createClientPacketHandler(IWorldConfig worldConfig) {
		PacketHandlerClient packetHandler = new PacketHandlerClient(worldConfig);
		channel.addListener(packetHandler::onPacket);
	}
}
