package mezz.jei.neoforge;

import mezz.jei.common.config.IServerConfig;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import mezz.jei.neoforge.network.NetworkHandler;

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
