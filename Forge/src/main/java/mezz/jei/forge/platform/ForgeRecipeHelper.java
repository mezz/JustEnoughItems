package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
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

    @Override
    public Ingredient getBase(UpgradeRecipe recipe) {
        return recipe.base;
    }

    @Override
    public Ingredient getAddition(UpgradeRecipe recipe) {
        return recipe.addition;
    }
}
