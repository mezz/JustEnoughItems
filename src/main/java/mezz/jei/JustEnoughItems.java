package mezz.jei;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;

import mezz.jei.api.constants.ModIds;
import mezz.jei.events.EventBusHelper;
import mezz.jei.startup.ClientLifecycleHandler;
import mezz.jei.startup.NetworkHandler;

@Mod(ModIds.JEI_ID)
public class JustEnoughItems {
	public JustEnoughItems() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		NetworkHandler networkHandler = new NetworkHandler();
		DistExecutor.runWhenOn(Dist.CLIENT, ()->()-> {
			EventBusHelper.addLifecycleListener(modEventBus, FMLLoadCompleteEvent.class, setupEvent -> {
				ClientLifecycleHandler clientLifecycleHandler = new ClientLifecycleHandler(networkHandler);
			});
		});
		EventBusHelper.addLifecycleListener(modEventBus, FMLCommonSetupEvent.class, event -> {
			networkHandler.createServerPacketHandler();
		});
	}
}
