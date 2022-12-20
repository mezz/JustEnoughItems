package mezz.jei.common.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.nio.file.Path;

public interface IPlatformHelper {
    <T> IPlatformRegistry<T> getRegistry(ResourceKey<? extends Registry<T>> key);

    IPlatformItemStackHelper getItemStackHelper();

    IPlatformFluidHelperInternal<?> getFluidHelper();

    IPlatformRenderHelper getRenderHelper();

    IPlatformRecipeHelper getRecipeHelper();

    IPlatformConfigHelper getConfigHelper();

    IPlatformInputHelper getInputHelper();

    IPlatformScreenHelper getScreenHelper();

    IPlatformIngredientHelper getIngredientHelper();

    IPlatformModHelper getModHelper();

    Path createConfigDir();
}
