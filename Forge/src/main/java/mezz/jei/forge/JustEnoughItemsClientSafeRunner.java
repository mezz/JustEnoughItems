package mezz.jei.forge;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.forge.network.NetworkHandler;

public class JustEnoughItemsClientSafeRunner {
	private final NetworkHandler networkHandler;
	private final PermanentEventSubscriptions subscriptions;
	private final IServerConfig serverConfig;

	public JustEnoughItemsClientSafeRunner(
		NetworkHandler networkHandler,
		PermanentEventSubscriptions subscriptions,
		IServerConfig serverConfig
	) {
		this.networkHandler = networkHandler;
		this.subscriptions = subscriptions;
		this.serverConfig = serverConfig;
	}

	public void registerClient() {
		JustEnoughItemsClient jeiClient = new JustEnoughItemsClient(networkHandler, subscriptions, serverConfig);
		jeiClient.register();
	}
}
