package mezz.jei.forge;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.ServerPacketRouter;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.network.ConnectionToClient;
import mezz.jei.forge.network.NetworkHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class JustEnoughItemsCommon {
	private final NetworkHandler networkHandler;
	private final IServerConfig serverConfig;

	public JustEnoughItemsCommon(NetworkHandler networkHandler, IServerConfig serverConfig) {
		this.networkHandler = networkHandler;
		this.serverConfig = serverConfig;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(FMLCommonSetupEvent.class, event -> this.commonSetup());
	}

	private void commonSetup() {
		IConnectionToClient connection = new ConnectionToClient();
		ServerPacketRouter packetRouter = new ServerPacketRouter(connection, serverConfig);
		this.networkHandler.registerServerPacketHandler(packetRouter);
	}
}
