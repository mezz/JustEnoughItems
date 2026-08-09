package mezz.jei.forge.platform;

import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
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
		IVanillaRecipeFactory vanillaRecipeFactory,
		IExtendableBrewingRecipeCategory brewingCategory
	) {
		Collection<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();
		Set<IJeiBrewingRecipe> recipes = new HashSet<>();

		addModdedBrewingRecipes(
			vanillaRecipeFactory,
			brewingRecipes,
			recipes,
			brewingCategory
		);

		List<IJeiBrewingRecipe> recipeList = new ArrayList<>(recipes);
		recipeList.sort(Comparator.comparingInt(IJeiBrewingRecipe::getBrewingSteps));
		return recipeList;
	}

	public static void addModdedBrewingRecipes(
		IVanillaRecipeFactory vanillaRecipeFactory,
		Collection<IBrewingRecipe> brewingRecipes,
		Collection<IJeiBrewingRecipe> recipes,
		IExtendableBrewingRecipeCategory brewingCategory
	) {
		Set<Class<?>> unhandledRecipeClasses = new HashSet<>();
		for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
			try {
				List<IJeiBrewingRecipe> extensionRecipes = brewingCategory.getBrewingRecipes(
					iBrewingRecipe,
					vanillaRecipeFactory
				);
				if (extensionRecipes != null) {
					recipes.addAll(extensionRecipes);
					continue;
				}
			} catch (RuntimeException | LinkageError e) {
				LOGGER.error(
					"Failed to handle custom brewing recipe class {}",
					iBrewingRecipe.getClass(),
					e
				);
				continue;
			}

			Class<?> recipeClass = iBrewingRecipe.getClass();
			if (unhandledRecipeClasses.add(recipeClass)) {
				LOGGER.debug("Can't handle brewing recipe class: {}", recipeClass);
			}
		}
	}
}
