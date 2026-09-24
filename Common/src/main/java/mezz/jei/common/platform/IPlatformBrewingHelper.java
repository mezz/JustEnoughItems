package mezz.jei.common.platform;

import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.recipes.BrewingExtensionHelper;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.alchemy.PotionBrewing;

import java.util.List;

public interface IPlatformBrewingHelper {
	/**
	 * Register handlers for platform brewing recipe objects.
	 * Platforms that expose brewing mixtures directly have no extensions to register.
	 */
	default void registerCategoryExtensions(
		IExtendableBrewingRecipeCategory brewingCategory,
		IIngredientManager ingredientManager
	) {
	}

	/**
	 * Discover the platform's brewing recipes and convert them for JEI.
	 */
	List<IJeiBrewingRecipe> getBrewingRecipes(
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		PotionBrewing potionBrewing,
		ContextMap contextMap,
		BrewingExtensionHelper brewingExtensionHelper
	);
}
