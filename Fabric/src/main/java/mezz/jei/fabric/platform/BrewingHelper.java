package mezz.jei.fabric.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformBrewingHelper;
import mezz.jei.common.recipes.BrewingExtensionHelper;
import mezz.jei.library.util.BrewingRecipeMakerCommon;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BrewingHelper implements IPlatformBrewingHelper {
	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		Collection<RecipeHolder<BrewingRecipe>> brewingRecipes,
		ContextMap contextMap,
		BrewingExtensionHelper brewingExtensionHelper
	) {
		return new ArrayList<>(
			BrewingRecipeMakerCommon.getVanillaBrewingRecipes(
				vanillaRecipeFactory,
				ingredientManager,
				brewingRecipes,
				contextMap
			)
		);
	}
}
