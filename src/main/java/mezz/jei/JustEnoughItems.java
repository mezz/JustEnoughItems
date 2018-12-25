package mezz.jei;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;

import mezz.jei.config.Constants;
import mezz.jei.startup.ClientLifecycleHandler;
import mezz.jei.startup.ServerLifecycleHandler;

@Mod(Constants.MOD_ID)
public class JustEnoughItems {
	public JustEnoughItems() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			new ClientLifecycleHandler();
		} else {
			new ServerLifecycleHandler();
		}
	}
}
