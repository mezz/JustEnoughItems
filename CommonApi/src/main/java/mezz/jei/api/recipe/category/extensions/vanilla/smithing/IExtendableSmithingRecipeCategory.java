package mezz.jei.api.recipe.category.extensions.vanilla.smithing;

import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.world.item.crafting.SmithingRecipe;

/**
 * Allows extending the vanilla smithing recipe category,
 * to support custom recipes classes that cannot be handled by default.
 *
 * Get the instance from {@link IVanillaCategoryExtensionRegistration#getSmithingCategory()}
 *
 * @since 15.12.0
 */
public interface IExtendableSmithingRecipeCategory {
	/**
	 * Add an extension that handles a subset of the recipes in the recipe category.
	 *
	 * @param recipeClass  the subset class of crafting recipes to handle
	 * @param extension    an extension for handling these recipes
	 * @since 15.12.0
	 */
	<R extends SmithingRecipe> void addExtension(
		Class<? extends R> recipeClass,
		ISmithingCategoryExtension<R> extension
	);
}
