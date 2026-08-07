package mezz.jei.fabric.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.util.BrewingRecipeMakerCommon;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BrewingRecipeMaker {
	public static List<IJeiBrewingRecipe> getBrewingRecipes(
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		Collection<RecipeHolder<BrewingRecipe>> brewingRecipes,
		ContextMap contextMap
	) {
		List<IJeiBrewingRecipe> recipes = BrewingRecipeMakerCommon.getVanillaBrewingRecipes(
			vanillaRecipeFactory,
			ingredientManager,
			brewingRecipes,
			contextMap
		);
		return recipes.stream()
			.sorted(Comparator.comparingInt(IJeiBrewingRecipe::getBrewingSteps))
			.toList();
	}
}
