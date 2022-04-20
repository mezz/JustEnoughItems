package mezz.jei.forge.plugins.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.forge.plugins.forge.brewing.BrewingRecipeMaker;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@JeiPlugin
public class ForgePlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "forge");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getIngredientManager();
        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();

        List<IJeiBrewingRecipe> brewingRecipes = BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory);
        registration.addRecipes(RecipeTypes.BREWING, brewingRecipes);
    }
}
