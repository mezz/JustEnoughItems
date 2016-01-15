package mezz.jei.api;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

/**
 * IJeiHelpers provides helpers and tools for addon mods.
 * Available to IModPlugins
 */
public interface IJeiHelpers {
	/**
	 * Helps with the implementation of GUIs.
	 */
	@Nonnull
	IGuiHelper getGuiHelper();

	/**
	 * Helps with getting itemStacks from recipes.
	 */
	@Nonnull
	IStackHelper getStackHelper();

	/**
	 * Used to stop JEI from displaying a specific item in the item list.
	 */
	@Nonnull
	IItemBlacklist getItemBlacklist();

	/**
	 * Used to tell JEI to ignore NBT tags when comparing items for recipes.
	 */
	@Nonnull
	INbtIgnoreList getNbtIgnoreList();

	/**
	 * Helps with the implementation of Recipe Transfer Handlers
	 */
	@Nonnull
	IRecipeTransferHandlerHelper recipeTransferHandlerHelper();

	/**
	 * Reload JEI at runtime.
	 * Used by mods that add and remove items or recipes like MineTweaker's /mt reload.
	 */
	void reload();
}
