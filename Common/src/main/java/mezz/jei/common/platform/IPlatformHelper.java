package mezz.jei.common.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface IPlatformHelper {
    <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key);

    IPlatformItemStackHelper getItemStackHelper();

    IPlatformRenderHelper getRenderHelper();

    IPlatformRecipeHelper getRecipeHelper();

    IPlatformServerHelper getServerHelper();

    IPlatformConfigHelper getConfigHelper();
}
