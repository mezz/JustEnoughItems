package mezz.jei.neoforge.platform;

import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IBrewingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.recipes.BrewingExtensionHelper;
import mezz.jei.library.util.BrewingRecipeMakerCommon;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrewingRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();

	public static List<IJeiBrewingRecipe> getBrewingRecipes(
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		PotionBrewing potionBrewing,
		ContextMap contextMap,
		BrewingExtensionHelper brewingExtensionHelper
	) {
		Collection<IBrewingRecipe> brewingRecipes = potionBrewing.getRecipes();

		Set<IJeiBrewingRecipe> recipes = BrewingRecipeMakerCommon.getVanillaBrewingRecipes(
			vanillaRecipeFactory,
			ingredientManager,
			potionBrewing,
			contextMap
		);

		addModdedBrewingRecipes(
			vanillaRecipeFactory,
			brewingRecipes,
			recipes,
			contextMap,
			brewingExtensionHelper
		);

		List<IJeiBrewingRecipe> recipeList = new ArrayList<>(recipes);
		recipeList.sort(Comparator.comparingInt(IJeiBrewingRecipe::getBrewingSteps));

		return recipeList;
	}

	public static void addModdedBrewingRecipes(
		IVanillaRecipeFactory vanillaRecipeFactory,
		Collection<IBrewingRecipe> brewingRecipes,
		Collection<IJeiBrewingRecipe> recipes,
		ContextMap contextMap,
		BrewingExtensionHelper brewingExtensionHelper
	) {
		Set<Class<?>> unhandledRecipeClasses = new HashSet<>();
		for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
			IBrewingCategoryExtension<? super IBrewingRecipe> extension = brewingExtensionHelper.getRecipeExtension(iBrewingRecipe);
			if (extension != null) {
				try {
					List<IJeiBrewingRecipe> extensionRecipes = extension.getBrewingRecipes(
						iBrewingRecipe,
						vanillaRecipeFactory,
						contextMap
					);
					recipes.addAll(extensionRecipes);
				} catch (RuntimeException | LinkageError e) {
					LOGGER.error(
						"Failed to handle custom brewing recipe class {} with extension {}",
						iBrewingRecipe.getClass(),
						extension.getClass(),
						e
					);
				}
				continue;
			}

			Class<?> recipeClass = iBrewingRecipe.getClass();
			if (unhandledRecipeClasses.add(recipeClass)) {
				LOGGER.debug("Can't handle brewing recipe class: {}", recipeClass);
			}
		}
	}
}
