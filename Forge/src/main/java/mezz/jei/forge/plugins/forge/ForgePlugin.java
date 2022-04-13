package mezz.jei.forge.plugins.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.render.FluidTankRenderer;
import mezz.jei.forge.platform.FluidHelper;
import mezz.jei.forge.plugins.forge.brewing.BrewingRecipeMaker;
import mezz.jei.forge.plugins.forge.ingredients.fluid.FluidStackHelper;
import mezz.jei.forge.plugins.forge.ingredients.fluid.FluidStackListFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

@JeiPlugin
public class ForgePlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModIds.JEI_ID, "forge");
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        ISubtypeManager subtypeManager = registration.getSubtypeManager();
        IColorHelper colorHelper = registration.getColorHelper();

        List<FluidStack> fluidStacks = FluidStackListFactory.create();
        FluidStackHelper fluidStackHelper = new FluidStackHelper(subtypeManager, colorHelper);
        FluidTankRenderer<FluidStack> fluidTankRenderer = new FluidTankRenderer<>(FluidHelper.INSTANCE);
        registration.register(ForgeTypes.FLUID_STACK, fluidStacks, fluidStackHelper, fluidTankRenderer);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getIngredientManager();
        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();

        List<IJeiBrewingRecipe> brewingRecipes = BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory);
        registration.addRecipes(RecipeTypes.BREWING, brewingRecipes);
    }
}
