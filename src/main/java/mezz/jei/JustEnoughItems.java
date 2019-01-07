package mezz.jei;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;

import mezz.jei.api.ModIds;
import mezz.jei.events.EventBusHelper;
import mezz.jei.startup.ClientLifecycleHandler;
import mezz.jei.startup.ServerLifecycleHandler;

@Mod(ModIds.JEI_ID)
public class JustEnoughItems {
	public JustEnoughItems() {
		IEventBus modEventBus = FMLModLoadingContext.get().getModEventBus();
		DistExecutor.runWhenOn(Dist.CLIENT, ()->()-> {
			EventBusHelper.addLifecycleListener(modEventBus, FMLInitializationEvent.class, event -> {
				new ClientLifecycleHandler();
			});
		});
		EventBusHelper.addLifecycleListener(modEventBus, FMLInitializationEvent.class, event -> {
			new ServerLifecycleHandler();
		});
	}
}
