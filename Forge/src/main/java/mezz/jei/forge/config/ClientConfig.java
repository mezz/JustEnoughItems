package mezz.jei.forge.config;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ClientConfig {
    public static void register(ModLoadingContext modLoadingContext) {

        IConfigSpec<?> cfg;
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, cfg);
    }
}
