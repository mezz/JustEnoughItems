package mezz.jei.api;

import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

/**
 * IJeiHelpers provides helpers and tools for addon mods.
 * Get the instance from {@link IModRegistry#getJeiHelpers()}.
 */
public interface IJeiHelpers {
	/**
	 * Helps with the implementation of GUIs.
	 */
	IGuiHelper getGuiHelper();

	/**
	 * Helps with getting itemStacks from recipes.
	 */
	IStackHelper getStackHelper();

	/*
	 * Used to stop JEI from displaying a specific ingredient in the ingredient list
	 * @since JEI 4.2.1
	 */
	IIngredientBlacklist getIngredientBlacklist();

	/**
	 * Helps with the implementation of Recipe Transfer Handlers
	 */
	IRecipeTransferHandlerHelper recipeTransferHandlerHelper();

	/**
	 * Allows manual creation of vanilla recipes.
	 */
	IVanillaRecipeFactory getVanillaRecipeFactory();

	// DEPRECATED BELOW

	/**
	 * Used to stop JEI from displaying a specific item in the item list.
	 *
	 * @deprecated since JEI 4.2.1. Use {@link #getIngredientBlacklist()}.
	 */
	@Deprecated
	IItemBlacklist getItemBlacklist();

	/**
	 * Reload JEI at runtime.
	 * Used by mods that add and remove items or recipes like MineTweaker's /mt reload.
	 *
	 * @deprecated since JEI 4.2.2. This has been deactivated because it takes too long and causes server disconnects.
	 */
	@Deprecated
	void reload();
}
