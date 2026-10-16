package mezz.jei.api.recipe.category.extensions.vanilla.cooking;

import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows extending a vanilla cooking recipe category,
 * to support custom recipe classes that cannot be handled by default.
 *
 * Get the instance from {@link IVanillaCategoryExtensionRegistration#getSmeltingCategory()},
 * {@link IVanillaCategoryExtensionRegistration#getBlastingCategory()},
 * {@link IVanillaCategoryExtensionRegistration#getSmokingCategory()}, or
 * {@link IVanillaCategoryExtensionRegistration#getCampfireCookingCategory()}.
 *
 * @since 31.15.0
 */
@ApiStatus.NonExtendable
public interface IExtendableCookingRecipeCategory {
	/**
	 * Add an extension that handles a subset of the recipes in the recipe category.
	 * An extension registered for the recipe's exact runtime class is preferred.
	 * Otherwise, JEI uses the unique most-specific registered supertype.
	 * Recipes with multiple unrelated matching extensions are not handled.
	 *
	 * Only matching recipes in this category are passed to the extension.
	 * The same extension instance may be registered with other cooking categories independently.
	 * When no extension matches, JEI uses the vanilla recipe display.
	 *
	 * @param recipeClass a subclass of {@link AbstractCookingRecipe}, or an interface implemented by such recipes
	 * @param extension an extension for handling these recipes
	 * @param <R> the recipe class or shared interface
	 * @throws IllegalArgumentException if the class is not a cooking recipe class or interface, or is already registered
	 * @since 31.15.0
	 */
	<R> void addExtension(Class<? extends R> recipeClass, ICookingCategoryExtension<R> extension);
}
