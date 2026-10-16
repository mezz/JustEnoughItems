package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.ICookingCategoryExtension;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.IExtendableCookingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import org.jetbrains.annotations.ApiStatus;

/**
 * This allows you to register extensions to vanilla recipe categories, to customize their behavior.
 *
 * An instance of this is passed to you mod's plugin in
 * {@link IModPlugin#registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration)}
 */
@ApiStatus.NonExtendable
public interface IVanillaCategoryExtensionRegistration {
	/**
	 * {@link IJeiHelpers} provides helpers and tools for addon mods.
	 *
	 * @since 13.1.0
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * Get the vanilla crafting category, to extend it with your own mod's crafting category extensions.
	 */
	IExtendableCraftingRecipeCategory getCraftingCategory();

	/**
	 * Get the vanilla smithing category, to extend it with your own mod's smithing category extensions.
	 * @since 19.5.0
	 */
	IExtendableSmithingRecipeCategory getSmithingCategory();

	/**
	 * Get the vanilla brewing category, to extend it with custom platform brewing recipe extensions.
	 * @since 30.17.0
	 */
	IExtendableBrewingRecipeCategory getBrewingCategory();

	/**
	 * Get the vanilla smelting category, to extend it with your own mod's cooking category extensions.
	 *
	 * @since 31.15.0
	 */
	IExtendableCookingRecipeCategory getSmeltingCategory();

	/**
	 * Get the vanilla blasting category, to extend it with your own mod's cooking category extensions.
	 *
	 * @since 31.15.0
	 */
	IExtendableCookingRecipeCategory getBlastingCategory();

	/**
	 * Get the vanilla smoking category, to extend it with your own mod's cooking category extensions.
	 *
	 * @since 31.15.0
	 */
	IExtendableCookingRecipeCategory getSmokingCategory();

	/**
	 * Get the vanilla campfire cooking category, to extend it with your own mod's cooking category extensions.
	 *
	 * @since 31.15.0
	 */
	IExtendableCookingRecipeCategory getCampfireCookingCategory();

	/**
	 * Register the same extension with smelting, blasting, smoking, and campfire cooking.
	 * Use the individual category getters to register extensions for a subset of these categories.
	 * Each category selects its extensions independently, following
	 * {@link IExtendableCookingRecipeCategory#addExtension(Class, ICookingCategoryExtension)}.
	 *
	 * @param recipeClass the cooking recipe class or shared interface to handle
	 * @param extension an extension for handling these recipes in all four cooking categories
	 * @param <R> the recipe class or shared interface
	 * @since 31.15.0
	 */
	default <R> void addCookingCategoryExtension(Class<? extends R> recipeClass, ICookingCategoryExtension<R> extension) {
		getSmeltingCategory().addExtension(recipeClass, extension);
		getBlastingCategory().addExtension(recipeClass, extension);
		getSmokingCategory().addExtension(recipeClass, extension);
		getCampfireCookingCategory().addExtension(recipeClass, extension);
	}
}
