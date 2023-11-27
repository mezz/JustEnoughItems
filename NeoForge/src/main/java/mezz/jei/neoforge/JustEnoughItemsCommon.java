package mezz.jei.neoforge;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.ServerPacketRouter;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import mezz.jei.neoforge.network.ConnectionToClient;
import mezz.jei.neoforge.network.NetworkHandler;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

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
		IConnectionToClient connection = new ConnectionToClient(this.networkHandler);
		ServerPacketRouter packetRouter = new ServerPacketRouter(connection, serverConfig);
		this.networkHandler.registerServerPacketHandler(packetRouter);
	}
}
