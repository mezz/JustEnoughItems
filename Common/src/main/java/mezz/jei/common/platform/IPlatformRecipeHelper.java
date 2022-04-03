package mezz.jei.common.platform;

import net.minecraft.world.item.crafting.CraftingRecipe;

public interface IPlatformRecipeHelper {
    <T extends CraftingRecipe> int getWidth(T recipe);
    <T extends CraftingRecipe> int getHeight(T recipe);
}
