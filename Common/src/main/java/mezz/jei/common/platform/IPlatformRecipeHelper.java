package mezz.jei.common.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.jetbrains.annotations.Nullable;

public interface IPlatformRecipeHelper {
    <T extends CraftingRecipe> int getWidth(T recipe);
    <T extends CraftingRecipe> int getHeight(T recipe);

    Ingredient getBase(UpgradeRecipe recipe);
    Ingredient getAddition(UpgradeRecipe recipe);

    @Nullable
    ResourceLocation getRegistryNameForRecipe(Object object);
}
