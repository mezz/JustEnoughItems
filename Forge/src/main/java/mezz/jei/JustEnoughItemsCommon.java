package mezz.jei;

import mezz.jei.forge.config.ServerConfig;
import mezz.jei.events.PermanentEventSubscriptions;
import mezz.jei.startup.NetworkHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class JustEnoughItemsCommon {
	private final NetworkHandler networkHandler;

	public JustEnoughItemsCommon(NetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}

	public void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(FMLCommonSetupEvent.class, event -> this.commonSetup());
	}

	private void commonSetup() {
		this.networkHandler.createServerPacketHandler();
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		ServerConfig.register(modLoadingContext);
	}
}
