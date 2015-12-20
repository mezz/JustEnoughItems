package mezz.jei.api;

import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

/**
 * IJeiHelpers provides helpers and tools for addon mods.
 * Available to IModPlugins
 */
public interface IJeiHelpers {
	/**
	 * Helps with the implementation of GUIs.
	 */
	IGuiHelper getGuiHelper();

	/**
	 * Used to stop JEI from displaying a specific item in the item list.
	 */
	IItemBlacklist getItemBlacklist();

	/**
	 * Used to tell JEI to ignore NBT tags when comparing items for recipes.
	 */
	INbtIgnoreList getNbtIgnoreList();

	/**
	 * Helps with the implementation of Recipe Transfer Handlers
	 */
	IRecipeTransferHandlerHelper recipeTransferHandlerHelper();
}
