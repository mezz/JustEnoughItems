package mezz.jei.fabric.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import java.util.List;
import java.util.Optional;

public class RecipeHelper implements IPlatformRecipeHelper {
    @Override
    public <T extends CraftingRecipe> int getWidth(T recipe) {
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            return shapedRecipe.getWidth();
        }
        return 0;
    }

    @Override
    public <T extends CraftingRecipe> int getHeight(T recipe) {
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            return shapedRecipe.getHeight();
        }
        return 0;
    }

    @SuppressWarnings("removal")
    @Override
    public Ingredient getBase(SmithingRecipe recipe) {
        if (recipe instanceof net.minecraft.world.item.crafting.LegacyUpgradeRecipe legacyRecipe) {
            return legacyRecipe.base;
        }
        if (recipe instanceof SmithingTransformRecipe transformRecipe) {
            return transformRecipe.base;
        }
        if (recipe instanceof SmithingTrimRecipe trimRecipe) {
            return trimRecipe.base;
        }
        return Ingredient.EMPTY;
    }

    @SuppressWarnings("removal")
    @Override
    public Ingredient getAddition(SmithingRecipe recipe) {
        if (recipe instanceof net.minecraft.world.item.crafting.LegacyUpgradeRecipe legacyRecipe) {
            return legacyRecipe.addition;
        }
        if (recipe instanceof SmithingTransformRecipe transformRecipe) {
            return transformRecipe.addition;
        }
        if (recipe instanceof SmithingTrimRecipe trimRecipe) {
            return trimRecipe.addition;
        }
        return Ingredient.EMPTY;
    }

    @SuppressWarnings("removal")
    @Override
    public boolean isHandled(SmithingRecipe recipe) {
        if (recipe.isIncomplete()) {
            return false;
        }
        return recipe instanceof net.minecraft.world.item.crafting.LegacyUpgradeRecipe ||
            recipe instanceof SmithingTransformRecipe ||
            recipe instanceof SmithingTrimRecipe;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public Optional<ResourceLocation> getRegistryNameForRecipe(Recipe<?> recipe) {
        ResourceLocation id = recipe.getId();
        return Optional.ofNullable(id);
    }

    @Override
    public List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory) {
        return BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory);
    }
}
