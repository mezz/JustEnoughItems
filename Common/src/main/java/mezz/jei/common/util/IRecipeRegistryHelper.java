package mezz.jei.common.util;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface IRecipeRegistryHelper {
    @Nullable
    ResourceLocation getRegistryNameForRecipe(Object recipe);
}
