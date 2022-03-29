package mezz.jei;

import mezz.jei.core.config.IServerConfig;
import mezz.jei.forge.config.ServerConfig;
import mezz.jei.events.PermanentEventSubscriptions;
import mezz.jei.forge.network.ConnectionToClient;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.forge.network.NetworkHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class JustEnoughItemsCommon {
	private final NetworkHandler networkHandler;
	private final IServerConfig serverConfig;

	public JustEnoughItemsCommon(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		serverConfig = ServerConfig.register(modLoadingContext);
	}

	public IServerConfig getServerConfig() {
		return serverConfig;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(FMLCommonSetupEvent.class, event -> this.commonSetup());
	}

	private void commonSetup() {
		IConnectionToClient connection = new ConnectionToClient();
		this.networkHandler.createServerPacketHandler(connection, serverConfig);
	}
}
