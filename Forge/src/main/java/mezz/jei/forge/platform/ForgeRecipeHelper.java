package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class ForgeRecipeHelper implements IPlatformRecipeHelper {
    @Override
    public <T extends CraftingRecipe> int getWidth(T recipe) {
        if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
            return shapedRecipe.getRecipeWidth();
        }
        return 0;
    }

    @Override
    public <T extends CraftingRecipe> int getHeight(T recipe) {
        if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
            return shapedRecipe.getRecipeHeight();
        }
        return 0;
    }
}
