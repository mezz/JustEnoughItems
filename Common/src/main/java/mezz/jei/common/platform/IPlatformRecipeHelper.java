package mezz.jei.common.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IPlatformRecipeHelper {
    <T extends CraftingRecipe> int getWidth(T recipe);
    <T extends CraftingRecipe> int getHeight(T recipe);

    Ingredient getBase(UpgradeRecipe recipe);
    Ingredient getAddition(UpgradeRecipe recipe);

    @Nullable
    ResourceLocation getRegistryNameForRecipe(Object object);

    List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory);
}
