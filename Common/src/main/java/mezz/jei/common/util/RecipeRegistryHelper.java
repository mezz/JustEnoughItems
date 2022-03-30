package mezz.jei.common.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public class RecipeRegistryHelper implements IRecipeRegistryHelper {
    @Override
    @Nullable
    public ResourceLocation getRegistryNameForRecipe(Object object) {
        if (object instanceof Recipe recipe) {
            return recipe.getId();
        }
        return null;
    }
}
