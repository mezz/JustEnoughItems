package mezz.jei.common.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingRecipe;

import java.util.List;
import java.util.Optional;

public interface IPlatformRecipeHelper {
    <T extends CraftingRecipe> int getWidth(T recipe);
    <T extends CraftingRecipe> int getHeight(T recipe);

    Ingredient getBase(SmithingRecipe recipe);
    Ingredient getAddition(SmithingRecipe recipe);
    Ingredient getTemplate(SmithingRecipe recipe);
    boolean isHandled(SmithingRecipe recipe);

    Optional<ResourceLocation> getRegistryNameForRecipe(Recipe<?> recipe);

    List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory);
}
