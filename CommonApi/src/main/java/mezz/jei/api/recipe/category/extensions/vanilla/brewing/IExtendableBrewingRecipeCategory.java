package mezz.jei.api.recipe.category.extensions.vanilla.brewing;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Allows extending the vanilla brewing recipe category,
 * to support custom platform recipe classes that cannot be handled by default.
 *
 * <p>
 * Get the instance from {@link IVanillaCategoryExtensionRegistration#getBrewingCategory()}.
 * </p>
 *
 * @since 11.39.0
 */
@ApiStatus.NonExtendable
public interface IExtendableBrewingRecipeCategory {
	/**
	 * Add an extension that handles a subset of the recipes in the brewing category.
	 *
	 * @param recipeClass the subset class of brewing recipes to handle
	 * @param extension an extension for handling these recipes
	 * @param <R> the custom platform brewing recipe type
	 * @since 11.39.0
	 */
	<R> void addExtension(
		Class<? extends R> recipeClass,
		IBrewingCategoryExtension<R> extension
	);

	/**
	 * Convert a platform brewing recipe with its registered extension.
	 *
	 * @param recipe the platform brewing recipe
	 * @param vanillaRecipeFactory factory for creating JEI brewing recipes
	 * @return the converted recipes, or null when no extension is registered for the recipe
	 * @param <R> the platform brewing recipe type
	 * @since 11.39.0
	 */
	@ApiStatus.Internal
	@Nullable
	<R> List<IJeiBrewingRecipe> getBrewingRecipes(
		R recipe,
		IVanillaRecipeFactory vanillaRecipeFactory
	);
}
