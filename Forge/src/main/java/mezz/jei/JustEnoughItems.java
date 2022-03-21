package mezz.jei;

import mezz.jei.api.constants.ModIds;
import mezz.jei.events.PermanentEventSubscriptions;
import mezz.jei.startup.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModIds.JEI_ID)
public class JustEnoughItems {
	public JustEnoughItems() {
		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		PermanentEventSubscriptions subscriptions = new PermanentEventSubscriptions(eventBus, modEventBus);

		NetworkHandler networkHandler = new NetworkHandler();

		JustEnoughItemsClient jeiClient = new JustEnoughItemsClient(networkHandler, subscriptions);
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> jeiClient::register);

		JustEnoughItemsCommon jeiCommon = new JustEnoughItemsCommon(networkHandler);
		jeiCommon.register(subscriptions);
	}
}
