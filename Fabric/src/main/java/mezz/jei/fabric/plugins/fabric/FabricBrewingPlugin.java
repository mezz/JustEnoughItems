package mezz.jei.fabric.plugins.fabric;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.library.util.BrewingRecipeMakerCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FabricBrewingPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "fabric_brewing");
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		List<IJeiBrewingRecipe> recipes = new ArrayList<>(
			BrewingRecipeMakerCommon.getVanillaBrewingRecipes(
				registration.getVanillaRecipeFactory(),
				registration.getIngredientManager(),
				FabricBrewingPlugin::getOutput
			)
		);
		recipes.sort(Comparator.comparingInt(IJeiBrewingRecipe::getBrewingSteps));
		registration.addRecipes(RecipeTypes.BREWING, recipes);
	}

	private static ItemStack getOutput(ItemStack input, ItemStack ingredient) {
		ItemStack result = PotionBrewing.mix(ingredient, input);
		if (result != input) {
			return result;
		}
		return ItemStack.EMPTY;
	}
}
