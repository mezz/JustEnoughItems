package mezz.jei;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;

import mezz.jei.api.ModIds;
import mezz.jei.events.EventBusHelper;
import mezz.jei.startup.ClientLifecycleHandler;
import mezz.jei.startup.NetworkHandler;

@Mod(ModIds.JEI_ID)
public class JustEnoughItems {
	public JustEnoughItems() {
		IEventBus modEventBus = FMLModLoadingContext.get().getModEventBus();
		NetworkHandler networkHandler = new NetworkHandler();
		EventBusHelper.addLifecycleListener(modEventBus, FMLClientSetupEvent.class, event -> {
			new ClientLifecycleHandler(networkHandler);
		});
		EventBusHelper.addLifecycleListener(modEventBus, FMLCommonSetupEvent.class, event -> {
			networkHandler.createServerPacketHandler();
		});
	}
}
