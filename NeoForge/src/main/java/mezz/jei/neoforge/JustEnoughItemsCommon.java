package mezz.jei.neoforge;

import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import mezz.jei.neoforge.network.ConnectionToClient;
import mezz.jei.neoforge.network.NetworkHandler;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class JustEnoughItemsCommon {
	private final NetworkHandler networkHandler;

	public JustEnoughItemsCommon(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(FMLCommonSetupEvent.class, event -> this.commonSetup(subscriptions));
	}

	private void commonSetup(PermanentEventSubscriptions subscriptions) {
		IConnectionToClient connection = new ConnectionToClient();
		this.networkHandler.registerServerPacketHandler(connection, subscriptions);
	}
}
