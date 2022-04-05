package mezz.jei.common.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface IPlatformHelper {
    <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key);

    IPlatformItemStackHelper getItemStackHelper();

    IPlatformFluidHelper<?> getFluidHelper();

    IPlatformRenderHelper getRenderHelper();

    IPlatformRecipeHelper getRecipeHelper();

    IPlatformServerHelper getServerHelper();

    IPlatformConfigHelper getConfigHelper();

    IPlatformInputHelper getInputHelper();

    IPlatformScreenHelper getScreenHelper();

    IPlatformIngredientHelper getIngredientHelper();
}
