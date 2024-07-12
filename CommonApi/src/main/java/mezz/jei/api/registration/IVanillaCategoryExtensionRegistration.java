package mezz.jei.api.registration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;

/**
 * This allows you to register extensions to vanilla recipe categories, to customize their behavior.
 *
 * An instance of this is passed to you mod's plugin in
 * {@link IModPlugin#registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration)}
 */
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
}
