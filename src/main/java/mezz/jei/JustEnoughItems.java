package mezz.jei;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;

import mezz.jei.api.ModIds;
import mezz.jei.events.EventBusHelper;
import mezz.jei.startup.ClientLifecycleHandler;
import mezz.jei.startup.ServerLifecycleHandler;

@Mod(ModIds.JEI_ID)
public class JustEnoughItems {
	public JustEnoughItems() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			EventBusHelper.addListener(FMLPreInitializationEvent.class, event -> {
				new ClientLifecycleHandler();
			});
		} else {
			EventBusHelper.addListener(FMLPreInitializationEvent.class, event -> {
				new ServerLifecycleHandler();
			});
		}
	}
}
