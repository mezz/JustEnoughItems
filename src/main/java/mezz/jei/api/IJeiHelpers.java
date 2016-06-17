package mezz.jei.api;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

/**
 * IJeiHelpers provides helpers and tools for addon mods.
 * Get it from IModRegistry, which is available to IModPlugins.
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
	 * @deprecated all nbt is now ignored by default. If you have nbt that is used to identify your item's subtype, see {@link #getSubtypeRegistry()}.
	 */
	@Nonnull
	@Deprecated
	INbtIgnoreList getNbtIgnoreList();

	/**
	 * If your item has subtypes that depend on NBT or capabilities, use this to help JEI identify those subtypes correctly.
	 */
	ISubtypeRegistry getSubtypeRegistry();

	/**
	 * @deprecated Use {@link #getSubtypeRegistry()}
	 */
	@Deprecated
	INbtRegistry getNbtRegistry();

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
