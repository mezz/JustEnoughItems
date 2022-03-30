package mezz.jei.forge.util;

import mezz.jei.common.util.IRecipeRegistryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public class ForgeRecipeRegistryHelper implements IRecipeRegistryHelper {
    @Override
    @Nullable
    public ResourceLocation getRegistryNameForRecipe(Object object) {
        if (object instanceof Recipe recipe) {
            return recipe.getId();
        } else if (object instanceof IForgeRegistryEntry<?> registryEntry) {
            return registryEntry.getRegistryName();
        }
        return null;
    }
}
