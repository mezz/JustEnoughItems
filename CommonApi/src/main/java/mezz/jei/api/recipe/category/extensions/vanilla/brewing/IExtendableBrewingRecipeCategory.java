package mezz.jei.api.recipe.category.extensions.vanilla.brewing;

import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows extending the vanilla brewing recipe category,
 * to support custom platform recipe classes that cannot be handled by default.
 *
 * <p>
 * Get the instance from {@link IVanillaCategoryExtensionRegistration#getBrewingCategory()}.
 * </p>
 *
 * @since 30.17.0
 */
@ApiStatus.NonExtendable
public interface IExtendableBrewingRecipeCategory {
	/**
	 * Add an extension that handles a subset of the recipes in the brewing category.
	 *
	 * @param recipeClass the subset class of brewing recipes to handle
	 * @param extension an extension for handling these recipes
	 * @param <R> the custom platform brewing recipe type
	 * @since 30.17.0
	 */
	<R> void addExtension(
		Class<? extends R> recipeClass,
		IBrewingCategoryExtension<R> extension
	);
}
