package mezz.jei.forge.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraftforge.common.crafting.IShapedRecipe;

import java.util.List;
import java.util.Optional;

public class RecipeHelper implements IPlatformRecipeHelper {
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

    @Override
    public Optional<ResourceLocation> getRegistryNameForRecipe(Object object) {
        if (object instanceof Recipe<?> recipe) {
            ResourceLocation id = recipe.getId();
            return Optional.of(id);
        }
        return Optional.empty();
    }

    @Override
    public List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory) {
        return BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory);
    }
}
